package cz.payola.web.client.views.plugins.visual.graph

import collection.mutable.ListBuffer
import s2js.adapters.js.dom.{Element, CanvasRenderingContext2D}
import cz.payola.common.rdf.{Vertex, Graph}
import cz.payola.web.client.views.plugins.visual.{Vector, RedrawOperation, Color, Point}

/**
  * Graphical representation of Graph object.
  * @param graphModel the graph object from the model
  * @param container the space where the graph should be visualised
  */
class GraphView(val graphModel: Graph, container: Element) extends View
{
    private val ColorVertexHigh = new Color(200, 0, 0, 1)

    private val ColorVertexMedium = new Color(0, 180, 0, 0.9)

    private val ColorVertexLow = new Color(180, 180, 180, 0.3)

    private val ColorTextHigh = new Color(50, 50, 50, 1)

    /*The order in which are layers created determines their "z coordinate"
(first created layer is on the bottom and last created one covers all the others).*/
    private val edgesDeselectedLayer = createLayer(container)

    private val edgesDeselectedTextLayer = createLayer(container)

    private val edgesSelectedLayer = createLayer(container)

    private val edgesSelectedTextLayer = createLayer(container)

    private val verticesDeselectedLayer = createLayer(container)

    private val verticesDeselectedTextLayer = createLayer(container)

    private val verticesSelectedLayer = createLayer(container)

    private val verticesSelectedTextLayer = createLayer(container)

    /**
      * Topmost layer to contain listeners, controls of the visualisation.
      */
    val controlsLayer = createLayer(container)

    private val layers = List(
        edgesDeselectedLayer, edgesDeselectedTextLayer, edgesSelectedLayer, edgesSelectedTextLayer,
        verticesDeselectedLayer, verticesDeselectedTextLayer, verticesSelectedLayer, verticesSelectedTextLayer,
        controlsLayer)

    /**
      * List of currently visualised vertices of the graph.
      */
    val vertexViews = createVertexViews()

    /**
      * List of currently visualised edges of the graph
      */
    val edgeViews = createEdges(vertexViews)

    private var selectedCount = 0

    /**
      * Empty graph indication function.
      * @return true if no vertices are present in this.vertexViews variable
      */
    def isEmpty: Boolean = {
        vertexViews.length == 0
    }

    /**
      * Constructs a list of vertexViews based on the graphModel variable.
      * @return container with packed Vertex objects in VertexView objects
      */
    def createVertexViews(): ListBuffer[VertexView] = {
        val buffer = ListBuffer[VertexView]()
        var counter = 0

        graphModel.vertices.foreach {vertexModel =>

            buffer += new VertexView(vertexModel, Point(0, 0))
            counter += 1
        }
        buffer
    }

    /**
      * Constructs a list of edgeViews based on the graphModel and verticesView variables.
      * Also modifies the vertexViews and sets the constructed edges to their vertexView.edges
      * attributes.
      * @param _vertexViews list of vertexViews in which to search for vertexViews,
      * that are supposed to be connected by the created edgeViews
      * @return container with packed
      */
    def createEdges(_vertexViews: ListBuffer[VertexView]): ListBuffer[EdgeView] = {
        val buffer = ListBuffer[EdgeView]()
        if(_vertexViews.length != 0) {
            graphModel.edges.foreach {edgeModel =>
                buffer += new EdgeView(edgeModel, findVertexView(edgeModel.origin), findVertexView(edgeModel.destination))
            }

            _vertexViews.foreach {vertexView: VertexView =>
                vertexView.edges = getEdgesOfVertex(vertexView, buffer)
            }
        }

        buffer
    }

    /**
      * Searches for all edges in the _edgeViews parameter, that have the vertexView parameter as its origin or
      * destination and returns all these edges in a container.
      * @param vertexView to searche the edges container for
      * @param _edgeViews container of edges to search in
      * @return container fith found edges
      */
    private def getEdgesOfVertex(vertexView: VertexView, _edgeViews: ListBuffer[EdgeView]): ListBuffer[EdgeView] = {
        var edgeViewsBuffer = ListBuffer[EdgeView]()
        var i = 0
        //TODO may be used ...filter ?
        _edgeViews.foreach {_edgeView: EdgeView =>
            if ((_edgeView.originView.vertexModel eq vertexView.vertexModel) ||
                (_edgeView.destinationView.vertexModel eq vertexView.vertexModel)) {

                edgeViewsBuffer += _edgeView
                i += 1
            }
        }
        edgeViewsBuffer
    }

    /**
      * Adds the input vector to positions of all vertices in this graph visualisation.
      * @param difference to move the vertices
      */
    def moveAllSelectedVetrtices(difference: Vector) {
        vertexViews.foreach { vertex =>
            if(vertex.selected) {
                vertex.position += difference
            }
        }
    }

    /**
      * Marks all verices in this graph graphical representation as NOT selected.
      * @return true if a change of the selection status was changed on some vertex
      */
    def deselectAll(): Boolean = {
        var somethingChanged = false
        if (selectedCount > 0) {
            vertexViews.foreach {vertex =>
                somethingChanged = deselectVertex(vertex) || somethingChanged
            }
            selectedCount = 0
        }
        somethingChanged
    }

    /**
      * Marks the input vertex as NOT selected by calling setVertexSelection(vertex, false)
      * @param vertex to change its selected attribute
      * @return true if the selected attribute of the vertex has changed
      */
    def deselectVertex(vertex: VertexView): Boolean = {
        setVertexSelection(vertex, false)
    }

    /**
      * Marks the input vertex as selected by calling setVertexSelection(vertex, true)
      * @param vertex to change its selected attribute
      * @return true if the selected attribute of the vertex has changed
      */
    def selectVertex(vertex: VertexView): Boolean = {
        setVertexSelection(vertex, true)
    }

    /**
      * Switches the selected attribute of the input vertex to the opposite value.
      * @param vertex to switch its selected attribute
      * @return always true
      */
    def invertVertexSelection(vertex: VertexView): Boolean = {
        setVertexSelection(vertex, !vertex.selected)
    }

    /**
      * Setter of the selected atribute for vertices.
      * @param vertex to which is the selected value is set
      * @param selected new value to be set to the vertex
      * @return true if the value of the vertex.selected attribute is changed
      */
    def setVertexSelection(vertex: VertexView, selected: Boolean): Boolean = {
        if (vertex.selected != selected) {
            selectedCount += (if (selected) 1 else -1)
            vertex.selected = selected
            true
        } else {
            false
        }
    }

    /**
      * Finds a vertex in this graphs vertexViews container, that has the input point inside its graphical
      * (rectangular) representation.
      * @param point to compare the locations of vertices with
      * @return vertexView, that has the input point "inside", if none is found None
      */
    def getTouchedVertex(point: Point): Option[VertexView] = {
        vertexViews.find(v => v.isPointInside(point))
    }

    /**
      * Finds a vertexView that is a representation of the vertexModel object.
      * @param vertexModel to be searched
      * @return found graphica; representation of the input vertex from the model
      */
    private def findVertexView(vertexModel: Vertex): VertexView = {
        vertexViews.find(_.vertexModel.eq(vertexModel)).get
    }

    def draw(context: CanvasRenderingContext2D, color: Option[Color], position: Option[Point]) {
        val positionCorrection = position.getOrElse(Point.Zero)

        vertexViews.foreach {vertexView =>

            var colorToUseVertex: Option[Color] = Some(Color.Black)
            var colorToUseText: Option[Color] = Some(Color.Black)
            if (color != None) {
                colorToUseVertex = Some(color.get)
                colorToUseText = Some(color.get)
            } else if (vertexView.selected) {
                colorToUseVertex = Some(ColorVertexHigh)
                colorToUseText = Some(ColorTextHigh)
            } else if (edgeViews.exists(edgeView =>
                (edgeView.originView.eq(vertexView) && edgeView.destinationView.selected) ||
                    (edgeView.destinationView.eq(vertexView) && edgeView.originView.selected))) {
                colorToUseVertex = Some(ColorVertexMedium)
                colorToUseText = Some(ColorTextHigh)
            } else if (selectedCount == 0) {
                colorToUseVertex = None
                colorToUseText = Some(ColorTextHigh)
            } else {
                colorToUseVertex = Some(ColorVertexLow)
                colorToUseText = Some(Color.Transparent)
            }

            if (vertexView.selected) {
                if (verticesSelectedLayer.cleared) {
                    vertexView.draw(verticesSelectedLayer.context, colorToUseVertex, Some(positionCorrection))
                }
                if (verticesSelectedTextLayer.cleared && vertexView.information != None) {
                    vertexView.information.get.draw(verticesSelectedTextLayer.context, colorToUseText,
                        Some(LocationDescriptor.getVertexInformationPosition(vertexView.position) +
                            positionCorrection.toVector))
                }
            } else {
                if (verticesDeselectedLayer.cleared) {
                    vertexView.draw(verticesDeselectedLayer.context, colorToUseVertex, Some(positionCorrection))
                }
                if (verticesDeselectedTextLayer.cleared && vertexView.information != None) {
                    vertexView.information.get.draw(verticesDeselectedTextLayer.context, colorToUseText,
                        Some(LocationDescriptor.getVertexInformationPosition(vertexView.position) +
                            positionCorrection.toVector))
                }
            }
        }

        edgeViews.foreach {edgeView =>

            val positionToUse = LocationDescriptor.getEdgeInformationPosition(
                edgeView.originView.position, edgeView.destinationView.position) + positionCorrection.toVector
            val colorToUseEdge = color
            val colorToUseText = if (color != None) {
                color
            } else if (edgeView.isSelected) {
                Some(ColorTextHigh)
            } else {
                None
            }


            if(edgeView.areBothVerticesSelected) {
                edgeView.information.setSelectedForDrawing()
            }

            if (edgeView.isSelected) {
                if (edgesSelectedLayer.cleared) {
                    edgeView.draw(edgesSelectedLayer.context, colorToUseEdge, Some(positionCorrection))
                }
                if (edgesSelectedTextLayer.cleared && edgeView.areBothVerticesSelected) {
                    edgeView.information.draw(edgesSelectedTextLayer.context, colorToUseText, Some(positionToUse))
                }
            } else {
                if (edgesDeselectedLayer.cleared) {
                    edgeView.draw(edgesDeselectedLayer.context, colorToUseEdge, Some(positionCorrection))
                }
                if (edgesDeselectedTextLayer.cleared && edgeView.areBothVerticesSelected) {
                    edgeView.information.draw(edgesDeselectedTextLayer.context, colorToUseText, Some(positionToUse))
                }
            }
        }

        layers.foreach(layer => layer.cleared = false)
    }

    /**
      * Prepares the layers for drawing and calls the draw routine of this graph based on the graphOperation parameter.
      * If the Movement operation is used only layers for selected objects are redrawn. If the selection operation
      * is used all layers are redrawn.
      * @param graphOperation specifying which layers should be redrawn
      */
    def redraw(graphOperation: Int) {
        graphOperation match {
            case RedrawOperation.Movement =>
                clear(edgesSelectedLayer.context, Point.Zero, edgesSelectedLayer.getSize)
                edgesSelectedLayer.cleared = true
                clear(edgesSelectedTextLayer.context, Point.Zero, edgesSelectedTextLayer.getSize)
                edgesSelectedTextLayer.cleared = true
                clear(verticesSelectedLayer.context, Point.Zero, verticesSelectedLayer.getSize)
                verticesSelectedLayer.cleared = true
                clear(verticesSelectedTextLayer.context, Point.Zero, verticesSelectedTextLayer.getSize)
                verticesSelectedTextLayer.cleared = true

                draw(null, None, None)
            //^because elements are drawn into separate layers, redraw(..) does not know to which context to draw
            case RedrawOperation.Selection =>
                redrawAll()
            case _ =>
                redrawAll()
        }
    }

    /**
      * Redraws the whole graph from scratch. All layers are cleared and all elements of the graph are drawn again.
      */
    def redrawAll() {
        layers.foreach {layer =>
            clear(layer.context, Point.Zero, layer.getSize)
            layer.cleared = true
        }
        draw(null, None, None)
        //^because elements are drawn into separate layers, redraw(..) does not know to which context to draw
    }
}