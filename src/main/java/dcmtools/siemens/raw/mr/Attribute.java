package dcmtools.siemens.raw.mr;

public class Attribute implements Node {

    String type;
    String name;
    String value;

    @Override
    public final String type() {
        return this.type;
    }

    @Override
    public final String name() {
        return this.name;
    }

    public final String value() {
        return this.value;
    }
}
