# XJC plugin for Javadoc

XJC plugin to add **Javadoc** to generated JAXB classes (from `<xs:documentation>` XSD tags).

## Sample

Input XSD:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" targetNamespace="http://fr/pinguet62">
	<xs:complexType name="SampleModel">
		<xs:annotation>
			<xs:documentation>Class comment...</xs:documentation>
		</xs:annotation>
		<xs:sequence>
			<xs:element name="attr" type="xs:string">
				<xs:annotation>
					<xs:documentation>
						This is the best
						comment of the world!
					</xs:documentation>
				</xs:annotation>
			</xs:element>
		</xs:sequence>
	</xs:complexType>
</xs:schema>
```

Generated JAXB classes:
```java
// [...]

/**
 * Class comment...
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SampleModel", propOrder = {
    "attr"
})
public class SampleModel {

    /**
     * This is the best<br>
     * comment of the world!
     * 
     */
    @XmlElement(required = true)
    protected String attr;

    /**
     * This is the best<br>
     * comment of the world!
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttr() {
        return attr;
    }

    /**
     * This is the best<br>
     * comment of the world!
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttr(String value) {
        this.attr = value;
    }
}
```

## Usage

```xml
<project>
    <!-- ... -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.jvnet.jaxb2.maven2</groupId>
                <artifactId>maven-jaxb2-plugin</artifactId>
                <!-- ... -->
                <configuration>
                    <!-- ... -->
                    <args>
                        <arg>-Xjavadoc</arg>
                    </args>
                    <plugins>
                        <plugin>
                            <groupId>fr.pinguet62.xjc</groupId>
                            <artifactId>xjc-javadoc-plugin</artifactId>
                            <version>...</version>
                        </plugin>
                    </plugins>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## TODO

* *Formatter* (text, HTML, Markdown, ...)
* *Strategy* (replace, append, ...)
* *Target* (field, getter, setter, ...)
