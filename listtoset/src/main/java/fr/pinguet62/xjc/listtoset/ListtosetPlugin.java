package fr.pinguet62.xjc.listtoset;

import static com.sun.codemodel.JMod.PUBLIC;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

import fr.pinguet62.xjc.common.Utils;

public class ListtosetPlugin extends Plugin {

    public static final String _NAMESPACE_URI = "http://pinguet62.fr";

    public static final QName CHANGE_PROPERTY_TYPE_TO_SET = new QName(_NAMESPACE_URI, "typeSet");

    private static boolean processAll = false;

    @Override
    public List<String> getCustomizationURIs() {
        return Arrays.asList(_NAMESPACE_URI);
    }

    @Override
    public String getOptionName() {
        return "Xlisttoset";
    }

    @Override
    public String getUsage() {
        return "  -" + getOptionName() + "            : Change java.util.List properties to java.util.Set";
    }

    @Override
    public boolean isCustomizationTagName(String nsUri, String localName) {
        return new QName(nsUri, localName).equals(CHANGE_PROPERTY_TYPE_TO_SET);
    }

    /** Additional arguments must start with {@link #getOptionName()}, and key separated with {@code "-"} character. */
    @Override
    public int parseArgument(Options opt, String[] args, int start) throws BadCommandLineException, IOException {
        String nextArg = args[start];

        // No other args
        String prefix = "-" + getOptionName() + "-";
        if (!nextArg.startsWith(prefix))
            return 0;

        String arg = nextArg.substring(prefix.length());
        switch (arg) {
            case "processAll":
                processAll = true;
                break;
            default:
                throw new UnsupportedOperationException("Unknown argument: " + arg);
        }
        return 1;
    }

    private void processField(Outline outline, ClassOutline classOutline, FieldOutline fieldOutline) {
        // Field
        JFieldVar fieldVar = classOutline.implClass.fields().get(fieldOutline.getPropertyInfo().getName(false));

        if (!fieldVar.type().toString().contains("List<"))
            if (!processAll)
                throw new RuntimeException("Can only applied on maxOccurs=\"unbounded\" properties: "
                        + classOutline.implClass.name() + "#" + fieldVar.name());
            else
                return; // skip property

        List<JClass> genericArgs = ((JClass) fieldVar.type()).getTypeParameters();
        JClass setType = outline.getCodeModel().ref(Set.class).narrow(genericArgs);
        JClass hashsetType = outline.getCodeModel().ref(HashSet.class).narrow(genericArgs);

        // Field type
        fieldVar.type(setType);

        // Original getter
        String methodName = "get" + fieldOutline.getPropertyInfo().getName(true);
        JMethod originalGetter = Utils.getMethod(classOutline, methodName);
        JDocComment javadoc = originalGetter.javadoc();
        classOutline.implClass.methods().remove(originalGetter);

        // New getter
        JMethod newMethod = classOutline.implClass.method(PUBLIC, fieldVar.type(), methodName);
        newMethod.javadoc().add(javadoc);
        // impl
        JBlock body = newMethod.body();
        body._if(fieldVar.eq(JExpr._null()))._then().assign(fieldVar, JExpr._new(hashsetType));
        body._return(fieldVar);
    }

    @Override
    public boolean run(Outline outline, Options options, ErrorHandler errorHandler) throws SAXException {
        for (ClassOutline classOutline : outline.getClasses())
            for (FieldOutline fieldOutline : classOutline.getDeclaredFields())
                if (processAll)
                    processField(outline, classOutline, fieldOutline);
                else
                    for (CPluginCustomization customization : fieldOutline.getPropertyInfo().getCustomizations()) {
                        Element customizationElement = customization.element;
                        if (!new QName(customizationElement.getNamespaceURI(), customizationElement.getLocalName())
                                .equals(CHANGE_PROPERTY_TYPE_TO_SET))
                            continue;

                        processField(outline, classOutline, fieldOutline);
                        customization.markAsAcknowledged();
                    }
        return true;
    }

}