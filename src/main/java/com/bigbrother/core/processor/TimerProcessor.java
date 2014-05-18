package com.bigbrother.core.processor;

import com.bigbrother.core.annotations.Timer;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Options;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import javax.lang.model.util.SimpleAnnotationValueVisitor6;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.bigbrother.core.Commons.*;
import static com.sun.tools.javac.tree.JCTree.*;
import static javax.lang.model.SourceVersion.RELEASE_7;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({OPTION_BIG_BROTHER_ENABLED})
public final class TimerProcessor extends AbstractProcessor {
    private JavacProcessingEnvironment env;
    private JavaCompiler javaCompiler;
    private JavacElements utils;
    private JavacTypes types;
    private TreeMaker maker;
    private TreeInfo treeInfo;
    private Symtab symtab;
    private Names names;
    private Options options;

    private JavacTool tool = JavacTool.create();
    private JavacFileManager fm;

    private static final Multimap<LoggerMetaData, TimerMetaData> consumer2timersInfo = HashMultimap.create();

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.env = (JavacProcessingEnvironment) processingEnv;
        this.javaCompiler = JavaCompiler.instance(env.getContext());
        this.utils = env.getElementUtils();
        this.types = JavacTypes.instance(env.getContext());
        fm = env.getContext().get(JavacFileManager.class);
        this.maker = TreeMaker.instance(env.getContext());
        this.treeInfo = TreeInfo.instance(env.getContext());
        this.options = Options.instance(env.getContext());
        this.symtab = Symtab.instance(env.getContext());
        this.names = Names.instance(env.getContext());
    }

    private static final Set<String> supported_annotation_type = new HashSet<>();
    static {
        supported_annotation_type.add(Timer.class.getName());
//        supported_annotation_type.add(Catcher.class.getName());
//        supported_annotation_type.add(Counter.class.getName());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supported_annotation_type;
    }

    // not thread safe
    private static boolean generated = false;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!bigBrotherEnabled()) {
            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing skipped");
            return true;
        }

        if (annotations == null || annotations.isEmpty()) {
            return true;
        }

        processTimerElements(roundEnv.getElementsAnnotatedWith(Timer.class));

        /* not yet implemented */
//        processCounterElements(roundEnv.getElementsAnnotatedWith(Counter.class));

        /* not yet implemented */
//        processCatcherElements(roundEnv.getElementsAnnotatedWith(Catcher.class));

        generateClass(consumer2timersInfo.keySet());

        for (LoggerMetaData key : consumer2timersInfo.keySet()) {
            transformASTTimerMethods(key, consumer2timersInfo.get(key));
        }

        return true;
    }

    private void processTimerMethod(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                AnnotationValue val = entry.getValue();
                val.accept(new LoggerVisitor(), element);
            }
            for (Map.Entry<Symbol.MethodSymbol, Attribute> entry : utils.getElementValuesWithDefaults(mirror).entrySet()) {
                Attribute value = entry.getValue();
                value.accept(new LoggerVisitor(), element);
            }
        }
    }

    private void processTimerElements(Set<? extends Element> elements) {
        for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
                Timer annotation = element.getAnnotation(Timer.class);
                if (!annotation.enabled()) {
                    continue;
                }
                processTimerMethod(element);
            }
        }
    }

    private void processCatcherElements(Set<? extends Element> elements) {
        for (Element element : elements) {

        }
    }

    private void processCounterElements(Set<? extends Element> elements) {
        for (Element element : elements) {

        }
    }

    private boolean bigBrotherEnabled() {
        Map<String, String> options = env.getOptions();
        return options.containsKey(OPTION_BIG_BROTHER_ENABLED) && Boolean.parseBoolean(options.get(OPTION_BIG_BROTHER_ENABLED));
    }

    private void generateClass(Collection<LoggerMetaData> loggers) {
        if (!generated) {
            try {
                Filer filer = processingEnv.getFiler();
                String packageName =
                        Joiner.on('.')
                                .join(Lists.newArrayList(PACKAGE_NAME_COM, PACKAGE_NAME_BIGBROTHER, PACKAGE_NAME_CORE));
                JavaFileObject jof = filer.createSourceFile(packageName + '.' + CLASS_NAME_LOGGERS_ACCESS_POINT);
                PrintWriter writer = new PrintWriter(jof.openWriter(), false);

                writer.println("package " + packageName + ';');
                writer.println();

                writer.println("import " + packageName + ".Logger" + ';');
                writer.println();
                writer.println("import java.util.Collection" + ';');
                writer.println("import java.util.ArrayList" + ';');
                writer.println();

                writer.println("public final class " + CLASS_NAME_LOGGERS_ACCESS_POINT + " implements AccessPoint {");
                writer.println("private final Collection<Logger> loggers = new ArrayList<Logger>()" + ';');

                Collection<String> instanceNames = Lists.newArrayList();
                for (LoggerMetaData logger : loggers) {
                    instanceNames.add(logger.genInstanceName);
                    writer.println("public final Logger " + logger.genInstanceName + " = new " + logger.fullQualifiedClassName + "();");
                }

                writer.println("public " + CLASS_NAME_LOGGERS_ACCESS_POINT + "() {");
                for (String instanceName : instanceNames) {
                    writer.println("this.loggers.add(" + instanceName + ");");
                }
                writer.println("}");


                writer.println();

                writer.println("@Override");
                writer.println("public Collection<Logger> loggers() { return loggers; }");
                writer.println();

                writer.println("}");
                writer.println();

                writer.close();
            } catch (IOException ex) {
                throw new Error("Error generating source file");
            }
            generated = true;
        }
    }

    private void transformASTTimerMethods(LoggerMetaData loggerMetaData, Collection<TimerMetaData> timers) {
        for (TimerMetaData timerMetaData : timers) {
            JCMethodDecl method = (JCMethodDecl) utils.getTree(timerMetaData.element);
            tryFinallyBlockWrapper(loggerMetaData, timerMetaData);
            List<JCStatement> mStatements = List.nil();
            mStatements = mStatements.append(createStartTimeVariableStatement(timerMetaData));
            method.body.stats = mStatements.appendList(method.body.stats);
        }
    }

    @SupportedSourceVersion(RELEASE_7)
    class LoggerVisitor extends SimpleAnnotationValueVisitor6<Object, Element> {
        @Override
        protected Object defaultAction(Object o, Element element) {
            if (o instanceof TypeMirror) {
                TypeMirror mirror = (TypeMirror) o;
                if (mirror instanceof DeclaredType && mirror instanceof Type.ClassType) {
                    JCTree tree = utils.getTree(((DeclaredType) mirror).asElement());
                    LoggerMetaData loggerMetaData = new LoggerMetaData(o, tree);
                    Type.ClassType decl = (Type.ClassType) mirror;
                    TimerMetaData info = new TimerMetaData(element, decl.toString());
                    consumer2timersInfo.put(loggerMetaData, info);
                }
            }
            return null;
        }
    }

    private JCFieldAccess pkgCore() {
        JCIdent com = maker.Ident(utils.getName(PACKAGE_NAME_COM));
        JCFieldAccess bigbrother = maker.Select(com, utils.getName(PACKAGE_NAME_BIGBROTHER));
        return maker.Select(bigbrother, utils.getName(PACKAGE_NAME_CORE));
    }

    private JCExpression getConsumersAccessPointInstance(LoggerMetaData loggerMetaData) {
        JCFieldAccess consumersAccessPointGenClass =
                maker.Select(pkgCore(), utils.getName(CLASS_NAME_LOGGERS_ACCESS_POINT));

        JCFieldAccess bigbrotherClass =
                maker.Select(pkgCore(), utils.getName(CLASS_NAME_BIGBROTHER));

        JCMethodInvocation instanceMethodCall =
                maker.Apply(List.<JCExpression>nil(),
                            maker.Select(bigbrotherClass,
                            utils.getName(BIGBROTHER_METHOD_NAME_GET_INSTANCE)),
                            List.<JCExpression>nil());

        JCMethodInvocation getAccessPointMethodCall =
                maker.Apply(List.<JCExpression>nil(),
                            maker.Select(instanceMethodCall,
                            utils.getName(BIGBROTHER_METHOD_NAME_GET_ACCESS_POINT)),
                            List.<JCExpression>nil());

        return maker.Parens(maker.TypeCast(consumersAccessPointGenClass, getAccessPointMethodCall));
    }

    private JCVariableDecl varLoggersAccessPoint(LoggerMetaData consumer) {
        String varName = "loggersAccessPoint";
        return maker.VarDef(maker.Modifiers(Flags.FINAL),
                utils.getName(varName),
                maker.Select(pkgCore(), utils.getName(CLASS_NAME_LOGGERS_ACCESS_POINT)),
                getConsumersAccessPointInstance(consumer));
    }

    private JCVariableDecl varLogger(LoggerMetaData consumer, TimerMetaData timerMetaData) {
        JCFieldAccess loggersInstanceAccess =
                maker.Select(maker.Ident(utils.getName("loggersAccessPoint")), utils.getName(consumer.genInstanceName));

        return maker.VarDef(maker.Modifiers(Flags.FINAL),
                             utils.getName(timerMetaData.loggerVariableName),
                             maker.Select(pkgCore(), utils.getName(CLASS_NAME_LOGGER)),
                             loggersInstanceAccess
                );
    }

    private void tryFinallyBlockWrapper(LoggerMetaData loggerMetaData, TimerMetaData timerMetaData) {
        JCMethodDecl method = (JCMethodDecl) utils.getTree(timerMetaData.element);
        List<JCStatement> statements = method.body.stats;
        List<JCStatement> finallyStatements = List.nil();
        finallyStatements = finallyStatements.append(varLoggersAccessPoint(loggerMetaData));
        finallyStatements = finallyStatements.append(varLogger(loggerMetaData, timerMetaData));
        List<JCStatement> thenStatements = createThenStatements(loggerMetaData, timerMetaData);
        finallyStatements =
                finallyStatements
                        .append(checkConsumerNotNull(loggerMetaData,
                                timerMetaData,
                                maker.Block(Flags.BLOCK, thenStatements)));
        JCBlock finallyBlock = maker.Block(Flags.BLOCK, finallyStatements);
        JCTry tryTree = maker.Try(maker.Block(Flags.BLOCK, statements), List.<JCCatch>nil(), finallyBlock);
        List<JCStatement> mStatements = List.nil();
        mStatements = mStatements.append(tryTree);
        method.body.stats = mStatements;
    }

    private JCStatement checkConsumerNotNull(LoggerMetaData loggerMetaData, TimerMetaData timerMetaData, JCStatement then) {
        JCExpression cond =
                maker.Binary(JCTree.AND,
                    condConsumerNotNull(loggerMetaData, timerMetaData),
                    condConsumerEnabled(loggerMetaData, timerMetaData)
                );
        return maker.If(cond, then, null);
    }

    private JCExpression condConsumerNotNull(LoggerMetaData loggerMetaData, TimerMetaData timerMetaData) {
        return maker.Binary(JCTree.NE,
                maker.Ident(utils.getName(timerMetaData.loggerVariableName)),
                maker.Literal(TypeTags.BOT, null)
        );
    }

    private JCExpression condConsumerEnabled(LoggerMetaData loggerMetaData, TimerMetaData timerMetaData) {
        JCFieldAccess enabledMethod =
                maker.Select(maker.Ident(utils.getName(timerMetaData.loggerVariableName)),
                             utils.getName(loggerMetaData.enableMethodName));
        return maker.Apply(List.<JCExpression>nil(), enabledMethod, List.<JCExpression>nil());
    }

    private List<JCStatement> createThenStatements(LoggerMetaData loggerMetaData, TimerMetaData timerMetaData) {
        List<JCStatement> statements = List.nil();
        JCFieldAccess consumeMethod =
                maker.Select(maker.Ident(utils.getName(timerMetaData.loggerVariableName)),
                        utils.getName(loggerMetaData.logMethodName));
        List<JCExpression> args = List.nil();
        args = args.append(createDeltaTimeStatement(timerMetaData));
        JCMethodInvocation consumeMethodCall =
                maker.Apply(null, consumeMethod, args);
        statements = statements.append(maker.Exec(consumeMethodCall));
        return statements;
    }

    private JCStatement createStartTimeVariableStatement(TimerMetaData timer) {
        return maker.VarDef(maker.Modifiers(Flags.FINAL),
                            utils.getName(timer.startTimeVariableName),
                            maker.TypeIdent(TypeTags.LONG),
                            createCurrentTimeExpr(timer));
    }

    private JCExpression createDeltaTimeStatement(TimerMetaData timer) {
        return maker.Binary(JCTree.NEG, createCurrentTimeExpr(timer), maker.Ident(utils.getName(timer.startTimeVariableName)));
    }

    private JCExpression createCurrentTimeExpr(TimerMetaData info) {
        return maker.Apply(List.<JCExpression>nil(), timeFunctionSelection(info), List.<JCExpression>nil());
    }

    private JCExpression timeFunctionSelection(TimerMetaData info) {
        JCIdent system = maker.Ident(utils.getName("System"));
        Timer timer = info.element.getAnnotation(Timer.class);
        switch (timer.granularity()) {
            case MILLISECONDS:
                return maker.Select(system, utils.getName(METHOD_NAME_SYSTEM_CURRENT_TIME_MILLIS));
            case NANOSECONDS:
                return maker.Select(system, utils.getName(METHOD_NAME_SYSTEM_NANO_TIME));
        }
        throw new Error("Unknown time unit: " + timer.granularity());
    }
}
