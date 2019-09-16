package sk.csirt.viruschecker.client.web.template

import kotlinx.html.*

/**
 * Paragraph
 */
@HtmlTagMarker
fun FlowContent.pAlert(classes : String? = null,
                     block : P.() -> Unit = {}) : Unit =
    P(attributesMapOf("class", classes, "style", "color:red"), consumer).visit(block)

/**
 * Paragraph
 */
@HtmlTagMarker
fun FlowContent.pOk(classes : String? = null,
                     block : P.() -> Unit = {}) : Unit =
    P(attributesMapOf("class", classes, "style", "color:green"), consumer).visit(block)
