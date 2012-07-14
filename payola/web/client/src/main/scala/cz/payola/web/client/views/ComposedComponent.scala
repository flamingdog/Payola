package cz.payola.web.client.views

import s2js.adapters.js.dom

abstract class ComposedComponent extends Component
{
    private var _subComponents: Option[Seq[Component]] = None

    def createSubComponents: Seq[Component]

    def subComponents: Seq[Component] = {
        if (_subComponents.isEmpty) {
            _subComponents = Some(createSubComponents)
        }
        _subComponents.get
    }

    def render(parent: dom.Node) {
        subComponents.foreach(_.render(parent))
    }

    def destroy() {
        subComponents.foreach(_.destroy())
    }
}
