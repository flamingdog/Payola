package cz.payola.web.client.views

import s2js.adapters.js.dom.{CanvasRenderingContext2D, Canvas}

class Layer(val canvas: Canvas, val context: CanvasRenderingContext2D) {

    var cleared = true

    def setSize(size: Vector) {
        canvas.width = size.x;
        canvas.height = size.y;
    }
    
    def getSize: Vector = {
        Vector(canvas.width, canvas.height)
    }
}