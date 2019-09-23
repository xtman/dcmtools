package dcmtools.network;

public class KeyStoreSpec {

    private String _path;
    private String _type;
    private String _password;

    public KeyStoreSpec(String path, String type, String password) {
        _path = path;
        _type = type;
        _password = password;
    }

    public String path() {
        return _path;
    }

    public String type() {
        return _type;
    }

    public String password() {
        return _password;
    }

}
