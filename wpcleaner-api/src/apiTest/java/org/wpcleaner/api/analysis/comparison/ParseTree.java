package org.wpcleaner.api.analysis.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

record ParseTree(String text, List<ParseTreeElement> elements) {

  ParseTree {
    elements = List.copyOf(elements);
  }

  static ParseTree read(final String xml) {
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      final Element root =
          factory
              .newDocumentBuilder()
              .parse(new InputSource(new StringReader(xml)))
              .getDocumentElement();
      final Builder builder = new Builder();
      builder.visitChildren(root);
      return new ParseTree(builder.text.toString(), builder.elements);
    } catch (final ParserConfigurationException | SAXException | IOException e) {
      throw new IllegalStateException("Unable to read parse tree", e);
    }
  }

  List<ParseTreeElement> elements(final ParseTreeElement.Kind kind) {
    return elements.stream().filter(element -> element.kind() == kind).toList();
  }

  @SuppressWarnings("PMD.AvoidStringBufferField")
  private static final class Builder {

    private final StringBuilder text = new StringBuilder();
    private final List<ParseTreeElement> elements = new ArrayList<>();

    private void visit(final Node node) {
      if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
        text.append(node.getNodeValue());
        return;
      }
      if (node instanceof Element element) {
        visitElement(element);
      }
    }

    private void visitElement(final Element element) {
      switch (element.getTagName()) {
        case "template" -> visitBrackets(element, ParseTreeElement.Kind.TEMPLATE, "{{", "}}");
        case "tplarg" ->
            visitBrackets(element, ParseTreeElement.Kind.TEMPLATE_ARGUMENT, "{{{", "}}}");
        case "comment" -> visitRange(element, ParseTreeElement.Kind.COMMENT, 0);
        case "ignore" -> visitRange(element, ParseTreeElement.Kind.IGNORE, 0);
        case "h" ->
            visitRange(
                element,
                ParseTreeElement.Kind.HEADING,
                Integer.parseInt(element.getAttribute("level")));
        case "ext" -> visitExtensionTag(element);
        default -> visitChildren(element);
      }
    }

    private void visitChildren(final Node node) {
      for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
        visit(child);
      }
    }

    private void visitRange(
        final Element element, final ParseTreeElement.Kind kind, final int level) {
      final int begin = text.length();
      visitChildren(element);
      elements.add(new ParseTreeElement(kind, begin, text.length(), level));
    }

    private void visitBrackets(
        final Element element,
        final ParseTreeElement.Kind kind,
        final String open,
        final String close) {
      final int begin = text.length();
      text.append(open);
      for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
        if (child instanceof Element childElement && "part".equals(childElement.getTagName())) {
          text.append('|');
        }
        visit(child);
      }
      text.append(close);
      elements.add(new ParseTreeElement(kind, begin, text.length(), 0));
    }

    private void visitExtensionTag(final Element element) {
      final int begin = text.length();
      text.append('<').append(childText(element, "name")).append(childText(element, "attr"));
      if (findChild(element, "inner") == null) {
        text.append("/>");
      } else {
        text.append('>').append(childText(element, "inner")).append(childText(element, "close"));
      }
      elements.add(
          new ParseTreeElement(ParseTreeElement.Kind.EXTENSION_TAG, begin, text.length(), 0));
    }

    private static String childText(final Element element, final String tagName) {
      final Element child = findChild(element, tagName);
      return child == null ? "" : child.getTextContent();
    }

    @Nullable
    private static Element findChild(final Element element, final String tagName) {
      for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
        if (child instanceof Element childElement && tagName.equals(childElement.getTagName())) {
          return childElement;
        }
      }
      return null;
    }
  }
}
