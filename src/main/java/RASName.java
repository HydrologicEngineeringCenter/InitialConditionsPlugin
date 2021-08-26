public class RASName {

    private String _river;
    private String _reach;
    private String _STA;
    private String _USXS;
    private String _reservoirName;

    public String get_river() {
        return _river;
    }

    public String get_reach() {
        return _reach;
    }

    public String get_STA() {
        return _STA;
    }

    public String get_USXS() { return _USXS; }

    public String get_ReservoirName() {
        return _reservoirName;
    }

    public RASName(String river, String reach, String STA, String USXS, String reservoirName) {
        this._river = river;
        this._reach = reach;
        this._STA = STA;
        this._reservoirName = reservoirName;
        this._USXS = USXS;
    }
}

