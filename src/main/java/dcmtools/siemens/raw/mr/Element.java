package dcmtools.siemens.raw.mr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Element implements Node {

    String type;
    String name;
    List<Attribute> attributes;
    List<String> values;
    List<Element> elements;

    public final String type() {
        return this.type;
    }

    public final String name() {
        return this.name;
    }

    public final boolean hasName() {
        return this.name != null && !this.name.isEmpty();
    }

    public final List<Attribute> attributes() {
        return this.attributes == null ? null : Collections.unmodifiableList(this.attributes);
    }

    public final boolean hasAttribute() {
        return this.attributes != null && !this.attributes.isEmpty();
    }

    public final String value() {
        return (this.values == null || this.values.isEmpty()) ? null : this.values.get(0);
    }

    public final List<String> values() {
        return this.values == null ? null : Collections.unmodifiableList(this.values);
    }

    final void addValue(String value) {
        if (this.values == null) {
            this.values = new ArrayList<String>();
        }
        this.values.add(value);
    }

    public final boolean hasValue() {
        return this.values != null && !this.values.isEmpty();
    }

    public final List<Element> element() {
        return this.elements == null ? null : Collections.unmodifiableList(this.elements);
    }

    public final boolean hasElement() {
        return this.elements != null && !this.elements.isEmpty();
    }

}
