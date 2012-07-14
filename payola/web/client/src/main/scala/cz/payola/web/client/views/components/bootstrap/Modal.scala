package cz.payola.web.client.views.components.bootstrap

import s2js.compiler.javascript
import s2js.adapters.js.dom
import s2js.adapters.js.browser.document
import cz.payola.web.client.views._
import cz.payola.web.client.views.elements._
import cz.payola.web.client.events._
import scala.Some

/**
  * A modal popup window.
  * @param header Header of the modal.
  * @param body Components in the modal body.
  * @param saveText Save button text. If empty, the save button isn't shown.
  * @param cancelText Cancel button text. If empty, the cancel button isn't shown.
  * @param hasCloseButton Whether the close button should be shown.
  */
class Modal(
    val header: String,
    val body: Seq[Component] = Nil,
    val saveText: Option[String] = Some("Save changes"),
    val cancelText: Option[String] = Some("Cancel"),
    val hasCloseButton: Boolean = true)
    extends ComposedComponent
{
    /**
      * Triggered when the save button is clicked. The event handler should return whether the modal should be closed.
      */
    val saving = new BooleanEvent[this.type, EventArgs[this.type]]

    /**
      * Triggered when the cancel or close button is clicked. The event handler should return whether the modal should
      * be closed.
      */
    val closing = new BooleanEvent[this.type, EventArgs[this.type]]

    def createSubComponents = {
        val saveButton = new SpanButton(saveText.getOrElse(""), "btn-primary")
        val cancelButton = new SpanButton(cancelText.getOrElse(""))
        val closeButton = new Button("×", "close")

        saveButton.mouseClicked += { e => buttonClickedHandler(saving) }
        cancelButton.mouseClicked += { e => buttonClickedHandler(closing) }
        closeButton.mouseClicked += { e => buttonClickedHandler(closing) }

        List(new Div(List(
            new Div(
                (if (hasCloseButton) List(closeButton) else Nil) ++ List(new Heading(List(new Text(header)))),
                "modal-header"
            ),
            new Div(
                body,
                "modal-body"
            ),
            new Div(
                (if (cancelText.isDefined) List(cancelButton) else Nil) ++
                    (if (saveText.isDefined) List(saveButton) else Nil),
                "modal-footer"
            )
        ), "modal hide"))
    }

    override def render(parent: dom.Node = document.body) {
        super.render(parent)
        show()
    }

    override def destroy() {
        hide()
        super.destroy()
    }

    private def buttonClickedHandler(eventToTrigger: BooleanEvent[this.type, EventArgs[this.type]]): Boolean = {
        if (eventToTrigger.trigger(new EventArgs[this.type](this))) {
            destroy()
        }
        false
    }

    @javascript("$(self.subComponents().head().domElement).modal({ show: true, keyboard: false, backdrop: 'static' })")
    private def show() {}

    @javascript("jQuery(self.subComponents().head().domElement).modal('hide')")
    private def hide() {}
}
