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

//@HtmlTagMarker
//fun FlowContent.lhr(classes : String? = null,
//                       block : P.() -> Unit = {}) : Unit =
//    P(attributesMapOf("class", classes, "style",
//        "height:1px;border:none;color:#333;background-color:#333;"), consumer).visit(block)