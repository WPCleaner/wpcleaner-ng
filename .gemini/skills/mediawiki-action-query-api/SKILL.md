---
name: mediawiki-action-query-api
description:
  Implement a call to one API module of the MediaWiki action query API.
  Use when the user asks to implement a call to MediaWiki action query API.
---

# MediaWiki Action API Instructions

You act as a Java senior developer tasked with implementing a call to one API module of the MediaWiki action API.
In the instructions, the API module name (`modulename`) used as an example is `users`.
When this skill is active, you MUST:

1. Retrieve the documentation of the API module.
   The documentation is available at `https://www.mediawiki.org/w/api.php?action=help&modules=query%2Bmodulename`
   where `modulename` is the API module name.
   For the example, the document is available at `https://www.mediawiki.org/w/api.php?action=help&modules=query%2Busers`.
   Extract the text of the `h2` title in the documentation. It should follow the form `actiontype=modulename (moduleprefix)`
   where `actiontype` is the type of action of the module and `moduleprefix` the module prefix.
   For the example, the text is `list=users (us)`, so the type of action is `list` and the module prefix is `us`.

2. Compute the capitalized API module name, `ModuleName`.
   For the example, the capitalized API module name would be `Users`.
   For a module name `deletedrevisions` the capitalized API module name would be `DeletedRevisions` because `deleted` and `revisions` are separate words.

3. Implement in the existing `wpcleaner-api` Gradle module.
   Implement in Java package `org.wpcleaner.api.query.actiontype.modulename`
   where `actiontype` is the type of action and `modulename` is the API module name.
   For the example, the Java package would be `org.wpcleaner.api.query.list.users`.

4. Complete the existing class `QueryParameters` in package `org.wpcleaner.api.query` to add the API module.
 
5. Implement a Java enum named `ModuleNameParameters` where `ModuleName` is the capitalized API module name.
   For the example, the Java enum name would be `UsersParameters`.
   The Java enum should be defined with all available module parameters and nested enums for module parameters with a list possible values. 
   To obtain the name of the item:
   * Remove the `moduleprefix` from the parameter name (for the example, `usattachedwiki` becomes `attachedwiki`)
   * Split the words of the parameter name (for the example, `attachedwiki` becomes `attached wiki`)
   * If a word is an abbreviation, you can replace it with the full word (for the example, `prop` becomes `PROPERTIES`)
   * Convert the words to UPPER_SNAKE_CASE (for the example, `attached wiki` becomes `ATTACHED_WIKI`)
   
   For the example, one parameter in the documentation of the API module is named `usattachedwiki`, the corresponding item in the enum should `ATTACHED_WIKI`.
   The items in each Java enum should be sorted alphabetically.
   The nested enums should be sorted alphabetically.
 
6. As needed, implement records with proper Jackson annotations for modeling the response of the API module.
   When the API module has a properties parameter (like `usprop`), test all the possible values for it because the response may contain new fields for each value.
   Order record parameters alphabetically.
   Order record methods alphabetically.

7. If the API module has more the 5 parameters, implement a record for grouping all the available parameters.
   Order record parameters alphabetically.
   Implement a nested `Builder` class following the Builder pattern with the parameters sorted alphabetically, followed by the setter methods sorted alphabetically.
   If the API module has a parameter for specifying limits (like `uslimit`), ensure that you use a String and not an Integer for it, because it can be a number or the literal String `max`.

8. Implement the service class named `ApiModuleName` where `ModuleName` is the capitalized API module name.
 
9. Implement integration tests named `ApiModuleNameTest` where `ModuleName` is the capitalized API module name.
   Annotate the test class with `TestCallingMWApi`.
