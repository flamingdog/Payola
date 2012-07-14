package cz.payola.web.client.views.elements

import s2js.adapters.js.browser.document
import s2js.adapters.js.dom
import cz.payola.web.client.views.Component

class Text(initialValue: String) extends Component
{
    private var value = initialValue

    private var textNode: Option[dom.Element] = None

    private var parentElement: Option[dom.Element] = None

    def text: String = value

    def text_=(text: String) {
        value = text
        textNode.foreach { e =>
            destroy()
            parentElement.foreach(render(_))
        }
    }

    def render(parent: dom.Element) {
        parentElement = Some(parent)
        textNode = Some(document.createTextNode(value))
        parent.appendChild(textNode.get)
    }

    def destroy() {
        textNode.foreach(e => e.parentNode.removeChild(e))
    }
}