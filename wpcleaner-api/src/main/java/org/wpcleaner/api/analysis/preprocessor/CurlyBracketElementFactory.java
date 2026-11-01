package org.wpcleaner.api.analysis.preprocessor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.analysis.parameter.ParameterElement;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;
import org.wpcleaner.api.repository.magicword.MagicWord;

final class CurlyBracketElementFactory {

  private static final char FUNCTION_SEPARATOR = ':';
  private static final String FUNCTION_PREFIX = "#";
  private static final int PARAMETER_COUNT = 3;
  private static final Set<String> SUBST_MODIFIERS = Set.of("subst", "safesubst");
  private static final Set<String> MESSAGE_MODIFIERS = Set.of("msgnw", "msg");
  private static final Set<String> RAW_MODIFIERS = Set.of("raw");

  private final String text;
  private final List<MagicWord> magicWords;
  private final List<TemplateElement> templates;
  private final List<FunctionElement> functions;
  private final List<ParameterElement> parameters;

  CurlyBracketElementFactory(final String text, final List<MagicWord> magicWords) {
    this.text = text;
    this.magicWords = magicWords;
    this.templates = new ArrayList<>();
    this.functions = new ArrayList<>();
    this.parameters = new ArrayList<>();
  }

  List<TemplateElement> getTemplates() {
    return templates.stream().sorted(Element.COMPARATOR).toList();
  }

  List<FunctionElement> getFunctions() {
    return functions.stream().sorted(Element.COMPARATOR).toList();
  }

  List<ParameterElement> getParameters() {
    return parameters.stream().sorted(Element.COMPARATOR).toList();
  }

  void persist(final CurlyBrackets curlyBrackets, final List<CommentElement> comments) {
    final CommentedText commentedText =
        new CommentedText(
            text,
            comments.stream()
                .filter(
                    comment ->
                        comment.begin() >= curlyBrackets.begin()
                            && comment.end() <= curlyBrackets.end())
                .sorted(Element.COMPARATOR)
                .toList());
    if (curlyBrackets.count() == PARAMETER_COUNT) {
      persistParameter(curlyBrackets, commentedText);
    } else {
      persistTemplateOrFunction(curlyBrackets, commentedText);
    }
  }

  private void persistParameter(
      final CurlyBrackets curlyBrackets, final CommentedText commentedText) {
    final CurlyBrackets.Part namePart = curlyBrackets.parts().getFirst();
    final String defaultValue =
        curlyBrackets.parts().size() > 1 ? rawText(curlyBrackets.parts().get(1)) : null;
    parameters.add(
        new ParameterElement(
            curlyBrackets.begin(),
            curlyBrackets.end(),
            commentedText.withoutComments(namePart.begin(), namePart.end()).trim(),
            defaultValue));
  }

  private void persistTemplateOrFunction(
      final CurlyBrackets curlyBrackets, final CommentedText commentedText) {
    final CurlyBrackets.Part namePart = curlyBrackets.parts().getFirst();
    final List<String> modifiers = new ArrayList<>();
    int nameBegin =
        stripModifier(SUBST_MODIFIERS, namePart.begin(), namePart.end(), commentedText, modifiers);
    if (curlyBrackets.parts().size() == 1) {
      final String name = commentedText.withoutComments(nameBegin, namePart.end()).trim();
      final Optional<MagicWord> variable = findMagicWord(name, MagicWord::variable);
      if (variable.isPresent()) {
        functions.add(
            new FunctionElement(
                curlyBrackets.begin(),
                curlyBrackets.end(),
                name,
                variable.get().name(),
                List.of(),
                modifiers));
        return;
      }
    }
    nameBegin =
        stripModifier(MESSAGE_MODIFIERS, nameBegin, namePart.end(), commentedText, modifiers);
    nameBegin = stripModifier(RAW_MODIFIERS, nameBegin, namePart.end(), commentedText, modifiers);
    final int separator =
        commentedText.indexOutsideComments(FUNCTION_SEPARATOR, nameBegin, namePart.end());
    if (separator >= 0) {
      final String name = commentedText.withoutComments(nameBegin, separator).trim();
      final Optional<MagicWord> functionHook = findFunctionHook(name);
      if (functionHook.isPresent()) {
        persistFunction(
            curlyBrackets, new FunctionName(name, functionHook.get(), separator), modifiers);
        return;
      }
    }
    persistTemplate(curlyBrackets, commentedText, nameBegin, modifiers);
  }

  private int stripModifier(
      final Set<String> modifierNames,
      final int begin,
      final int end,
      final CommentedText commentedText,
      final List<String> modifiers) {
    final int aliasBegin = commentedText.skipWhitespaceAndComments(begin, end);
    for (final MagicWord magicWord : magicWords) {
      if (modifierNames.contains(magicWord.name())) {
        for (final String alias : magicWord.aliases()) {
          if (aliasBegin + alias.length() <= end
              && text.regionMatches(
                  !magicWord.caseSensitive(), aliasBegin, alias, 0, alias.length())) {
            modifiers.add(magicWord.name());
            return aliasBegin + alias.length();
          }
        }
      }
    }
    return begin;
  }

  private void persistFunction(
      final CurlyBrackets curlyBrackets,
      final FunctionName functionName,
      final List<String> modifiers) {
    final CurlyBrackets.Part namePart = curlyBrackets.parts().getFirst();
    final int separator = functionName.separator();
    final List<TemplateParameter> functionParameters = new ArrayList<>();
    functionParameters.add(
        new TemplateParameter(
            separator + 1, namePart.end(), null, text.substring(separator + 1, namePart.end())));
    curlyBrackets.parts().stream()
        .skip(1)
        .map(part -> new TemplateParameter(part.begin(), part.end(), null, rawText(part)))
        .forEach(functionParameters::add);
    functions.add(
        new FunctionElement(
            curlyBrackets.begin(),
            curlyBrackets.end(),
            functionName.name(),
            functionName.functionHook().name(),
            functionParameters,
            modifiers));
  }

  private void persistTemplate(
      final CurlyBrackets curlyBrackets,
      final CommentedText commentedText,
      final int nameBegin,
      final List<String> modifiers) {
    final CurlyBrackets.Part namePart = curlyBrackets.parts().getFirst();
    final String name = commentedText.withoutComments(nameBegin, namePart.end()).trim();
    if (name.isEmpty()) {
      return;
    }
    final List<TemplateParameter> templateParameters =
        curlyBrackets.parts().stream()
            .skip(1)
            .map(part -> createTemplateParameter(part, commentedText))
            .toList();
    templates.add(
        new TemplateElement(
            curlyBrackets.begin(), curlyBrackets.end(), name, templateParameters, modifiers));
  }

  private TemplateParameter createTemplateParameter(
      final CurlyBrackets.Part part, final CommentedText commentedText) {
    if (!part.hasEqual()) {
      return new TemplateParameter(part.begin(), part.end(), null, rawText(part));
    }
    return new TemplateParameter(
        part.begin(),
        part.end(),
        commentedText.withoutComments(part.begin(), part.equalIndex()).trim(),
        text.substring(part.equalIndex() + 1, part.end()));
  }

  private Optional<MagicWord> findFunctionHook(final String name) {
    final String nameWithoutPrefix =
        name.startsWith(FUNCTION_PREFIX) ? name.substring(FUNCTION_PREFIX.length()) : name;
    return findMagicWord(name, MagicWord::functionHook)
        .or(() -> findMagicWord(nameWithoutPrefix, MagicWord::functionHook));
  }

  private Optional<MagicWord> findMagicWord(final String name, final Predicate<MagicWord> filter) {
    return magicWords.stream()
        .filter(filter)
        .filter(magicWord -> magicWord.isPossibleAlias(name))
        .findFirst();
  }

  private String rawText(final CurlyBrackets.Part part) {
    return text.substring(part.begin(), part.end());
  }

  private record FunctionName(String name, MagicWord functionHook, int separator) {}

  private record CommentedText(String text, List<CommentElement> comments) {

    private String withoutComments(final int begin, final int end) {
      final StringBuilder result = new StringBuilder();
      int index = begin;
      for (final CommentElement comment : commentsBetween(begin, end)) {
        result.append(text, index, comment.begin());
        index = comment.end();
      }
      result.append(text, index, end);
      return result.toString();
    }

    private int indexOutsideComments(final char searched, final int begin, final int end) {
      int index = begin;
      for (final CommentElement comment : commentsBetween(begin, end)) {
        final int found = text.substring(index, comment.begin()).indexOf(searched);
        if (found >= 0) {
          return index + found;
        }
        index = comment.end();
      }
      final int found = text.substring(index, end).indexOf(searched);
      return found >= 0 ? index + found : -1;
    }

    private int skipWhitespaceAndComments(final int begin, final int end) {
      int index = begin;
      boolean skipped = true;
      while (skipped) {
        skipped = false;
        while (index < end && Character.isWhitespace(text.charAt(index))) {
          index++;
        }
        for (final CommentElement comment : comments) {
          if (comment.begin() == index && comment.end() <= end) {
            index = comment.end();
            skipped = true;
          }
        }
      }
      return index;
    }

    private List<CommentElement> commentsBetween(final int begin, final int end) {
      return comments.stream()
          .filter(comment -> comment.begin() >= begin && comment.end() <= end)
          .toList();
    }
  }
}
