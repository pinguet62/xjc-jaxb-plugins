package fr.pinguet62.xjc.swagger.option;

import static java.util.Arrays.asList;

import java.util.List;

import fr.pinguet62.xjc.common.argparser.RegexParser.Replacement;

public class DescriptionFormatting {

    public static final List<Replacement> DEFAULT = asList(
            // indentation
            new Replacement("^( |\t)+", ""), new Replacement("\n( |\t)+", "\n"),
            // empty lines: first & last
            new Replacement("^\r?\n+", ""), new Replacement("\r?\n+$", ""),
            // HTML
            new Replacement("\n", "<br>\n"));

}