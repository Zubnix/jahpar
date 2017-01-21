package org.freedesktop.jahpar.javacc;


import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.SetMultimap;
import org.freedesktop.jahpar.javacc.api.JavaCC;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParser;
import org.javacc.parser.MetaParseException;
import org.javacc.parser.NfaState;
import org.javacc.parser.Options;
import org.javacc.parser.ParseException;
import org.javacc.parser.Semanticize;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

public class JavaCCProcessingStep implements BasicAnnotationProcessor.ProcessingStep {

    private static class JavaCCValueVisitor extends SimpleAnnotationValueVisitor8<String, Void> {
        @Override
        protected String defaultAction(final Object o,
                                       final Void aVoid) {
            throw new AssertionError("Expected array value, instead got: " + o.toString());
        }

        @Override
        public String visitString(final String s,
                                  final Void aVoid) {
            return s;
        }
    }

    private final ProcessingEnvironment processingEnv;

    JavaCCProcessingStep(final ProcessingEnvironment processingEnv) {

        this.processingEnv = processingEnv;
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return Collections.singleton(JavaCC.class);
    }

    @Override
    public Set<Element> process(final SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        elementsByAnnotation.get(JavaCC.class)//get package element
                            .forEach((Element element) -> {
                                element.getAnnotationMirrors()
                                       .forEach(protocolsAnnotationMirror -> {//get package level annotations (@JavaCC)
                                           protocolsAnnotationMirror.getElementValues()
                                                                    .forEach((executableElement, protocolsAnnotationValue) -> {
                                                                        if (executableElement.getSimpleName()
                                                                                             .contentEquals("value")) {//get @JavaCC.value()
                                                                            final String file = protocolsAnnotationValue.accept(new JavaCCValueVisitor(),
                                                                                                                                null);
                                                                            processGrammar(file);
                                                                        }
                                                                    });
                                       });
                            });

        return Collections.emptySet();
    }

    private void processGrammar(final String file) {

    }

    public static void reInitAll()
    {
        org.javacc.parser.Expansion.reInit();
        org.javacc.parser.JavaCCErrors.reInit();
        org.javacc.parser.JavaCCGlobals.reInit();
        Options.init();
        org.javacc.parser.JavaCCParserInternals.reInit();
        org.javacc.parser.RStringLiteral.reInit();
        org.javacc.parser.JavaFiles.reInit();
        org.javacc.parser.NfaState.reInit();
        org.javacc.parser.MatchInfo.reInit();
        org.javacc.parser.LookaheadWalk.reInit();
        org.javacc.parser.Semanticize.reInit();
        org.javacc.parser.OtherFilesGen.reInit();
        org.javacc.parser.LexGen.reInit();
    }

    public static int mainProgram(String args[]) throws Exception {

        // Initialize all static state
        reInitAll();

        JavaCCGlobals.bannerLine("Parser Generator", "");

        JavaCCParser parser = null;
        if (args.length == 0) {
            System.out.println("");
            help_message();
            return 1;
        } else {
            System.out.println("(type \"javacc\" with no arguments for help)");
        }

        if (Options.isOption(args[args.length - 1])) {
            System.out.println("Last argument \"" + args[args.length-1] + "\" is not a filename.");
            return 1;
        }
        for (int arg = 0; arg < args.length-1; arg++) {
            if (!Options.isOption(args[arg])) {
                System.out.println("Argument \"" + args[arg] + "\" must be an option setting.");
                return 1;
            }
            Options.setCmdLineOption(args[arg]);
        }




        try {
            java.io.File fp = new java.io.File(args[args.length-1]);
            if (!fp.exists()) {
                System.out.println("File " + args[args.length-1] + " not found.");
                return 1;
            }
            if (fp.isDirectory()) {
                System.out.println(args[args.length-1] + " is a directory. Please use a valid file name.");
                return 1;
            }
            parser = new JavaCCParser(new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(args[args.length-1]), Options.getGrammarEncoding())));
        } catch (SecurityException se) {
            System.out.println("Security violation while trying to open " + args[args.length-1]);
            return 1;
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File " + args[args.length-1] + " not found.");
            return 1;
        }

        try {
            System.out.println("Reading from file " + args[args.length-1] + " . . .");
            JavaCCGlobals.fileName = JavaCCGlobals.origFileName = args[args.length-1];
            JavaCCGlobals.jjtreeGenerated = JavaCCGlobals.isGeneratedBy("JJTree", args[args.length-1]);
            JavaCCGlobals.toolNames = JavaCCGlobals.getToolNames(args[args.length-1]);
            parser.javacc_input();

            // 2012/05/02 - Moved this here as cannot evaluate output language
            // until the cc file has been processed. Was previously setting the 'lg' variable
            // to a lexer before the configuration override in the cc file had been read.
            String outputLanguage = Options.getOutputLanguage();
            // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
            boolean isJavaOutput = Options.isOutputLanguageJava();
            boolean isCPPOutput = outputLanguage.equals(Options.OUTPUT_LANGUAGE__CPP);

            // 2013/07/22 Java Modern is a
            boolean isJavaModern = isJavaOutput && Options.getJavaTemplateType().equals(Options.JAVA_TEMPLATE_TYPE_MODERN);

            if (isJavaOutput) {
                //TODO overwrite saveOutput() and use annotation processors file creation
                lg = new LexGen();
            } else if (isCPPOutput) {
                //TODO overwrite saveOutput() and use annotation processors file creation
                lg = new LexGenCPP();
            } else {
                return unhandledLanguageExit(outputLanguage);
            }

            JavaCCGlobals.createOutputDir(Options.getOutputDirectory());

            if (Options.getUnicodeInput())
            {
                NfaState.unicodeWarningGiven = true;
                System.out.println("Note: UNICODE_INPUT option is specified. " +
                                   "Please make sure you create the parser/lexer using a Reader with the correct character encoding.");
            }

            Semanticize.start();
            boolean isBuildParser = Options.getBuildParser();

            // 2012/05/02 -- This is not the best way to add-in GWT support, really the code needs to turn supported languages into enumerations
            // and have the enumerations describe the deltas between the outputs. The current approach means that per-langauge configuration is distributed
            // and small changes between targets does not benefit from inheritance.
            if (isJavaOutput) {
                if (isBuildParser) {
                    //TODO overwrite saveOutput() and use annotation processors file creation
                    new ParseGen().start(isJavaModern);
                }

                // Must always create the lexer object even if not building a parser.
                //TODO overwrite saveOutput() and use annotation processors file creation
                new LexGen().start();

                Options.setStringOption(Options.NONUSER_OPTION__PARSER_NAME, JavaCCGlobals.cu_name);
                OtherFilesGen.start(isJavaModern);
            } else if (isCPPOutput) { // C++ for now
                if (isBuildParser) {
                    //TODO overwrite saveOutput() and use annotation processors file creation
                    new ParseGenCPP().start();
                }
                if (isBuildParser) {
                    //TODO overwrite saveOutput() and use annotation processors file creation
                    new LexGenCPP().start();
                }
                Options.setStringOption(Options.NONUSER_OPTION__PARSER_NAME, JavaCCGlobals.cu_name);
                //TODO overwrite saveOutput() and use annotation processors file creation
                OtherFilesGenCPP.start();
            } else {
                unhandledLanguageExit(outputLanguage);
            }



            if ((JavaCCErrors.get_error_count() == 0) && (isBuildParser || Options.getBuildTokenManager())) {
                if (JavaCCErrors.get_warning_count() == 0) {
                    if (isBuildParser) {
                        System.out.println("Parser generated successfully.");
                    }
                } else {
                    System.out.println("Parser generated with 0 errors and "
                                       + JavaCCErrors.get_warning_count() + " warnings.");
                }
                return 0;
            } else {
                System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                                   + JavaCCErrors.get_warning_count() + " warnings.");
                return (JavaCCErrors.get_error_count()==0)?0:1;
            }
        } catch (MetaParseException e) {
            System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                               + JavaCCErrors.get_warning_count() + " warnings.");
            return 1;
        } catch (ParseException e) {
            System.out.println(e.toString());
            System.out.println("Detected " + (JavaCCErrors.get_error_count()+1) + " errors and "
                               + JavaCCErrors.get_warning_count() + " warnings.");
            return 1;
        }
    }

    private static int unhandledLanguageExit(String outputLanguage) {
        System.out.println("Invalid '" + Options.USEROPTION__OUTPUT_LANGUAGE+ "' specified : " + outputLanguage);
        return 1;
    }
}
