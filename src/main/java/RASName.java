public class RASName {

    private String _river;
    private String _reach;
    private String _XS;
    private String _reservoirName;

    public String get_river() {
        return _river;
    }

    public String get_reach() {
        return _reach;
    }

    public String get_XS() {
        return _XS;
    }

    public String get_ReservoirName() {
        return _reservoirName;
    }

    public RASName(String _river, String _reach, String _XS, String _reservoirName) {
        this._river = _river;
        this._reach = _reach;
        this._XS = _XS;
        this._reservoirName = _reservoirName;
    }
}

