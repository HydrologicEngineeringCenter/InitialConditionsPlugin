public class RASReservoir {


    private double _initialPool;
    private double _initialFlow;
    private double _minFlow;
    private String _RASname;
    private String _ResSimName;

    public double get_initialPool() {
        return _initialPool;
    }

    public double get_initialFlow() {
        return _initialFlow;
    }

    public double get_minFlow() {
        return _minFlow;
    }

    public String get_RASname() {
        return _RASname;
    }

    public String get_ResSimName() {
        return _ResSimName;
    }

    public void set_initialPool(double _initialPool) {
        this._initialPool = _initialPool;
    }

    public void set_initialFlow(double _initialFlow) {
        this._initialFlow = _initialFlow;
    }

    RASReservoir(String RASName, String ResSimName, double minFlow ){
        _minFlow = minFlow;
        _RASname = RASName;
        _ResSimName = ResSimName;
    }
}
