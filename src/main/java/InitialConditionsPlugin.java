/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.factories.NewObjectFactory;
import hec.data.Parameter;
import hec.model.OutputVariable;
import hec2.map.GraphicElement;
import hec2.model.DataLocation;
import hec2.model.ProgramOrderItem;
import hec2.plugin.action.EditAction;
import hec2.plugin.action.OutputElement;
import hec2.plugin.lang.ModelLinkingException;
import hec2.plugin.lang.OutputException;
import hec2.plugin.model.ModelAlternative;
import hec2.wat.model.tracking.OutputPlugin;
import hec2.wat.model.tracking.OutputVariableImpl;
import hec2.wat.plugin.AbstractSelfContainedWatPlugin;
import hec2.wat.plugin.CreatableWatPlugin;
import hec2.wat.plugin.WatPluginManager;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author WatPowerUser
 */
public class InitialConditionsPlugin extends AbstractSelfContainedWatPlugin<InitialConditionsAlternative> implements CreatableWatPlugin, OutputPlugin  {
    public static final String PluginName = "Initial Conditions";
    private static final String _pluginVersion = "1.0.0";
    private static final String _pluginSubDirectory = "IC";
    private static final String _pluginExtension = ".ic";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        InitialConditionsPlugin p = new InitialConditionsPlugin();
    }
    public InitialConditionsPlugin(){
        super();
        setName(PluginName);
        setProgramOrderItem(new ProgramOrderItem(PluginName,
                "A plugin to modify RAS initial conditions",
                false,1,"shortname","Images/fda/wsp.png"));
        WatPluginManager.register(this);
    }
    @Override
    protected String getAltFileExtension() {
        return _pluginExtension;
    }
    @Override
    public String getPluginDirectory() {
        return _pluginSubDirectory;
    }
    @Override
    public String getVersion() {
        return _pluginVersion;
    }
    @Override
    public boolean saveProject() {
        boolean success = true;
        for(InitialConditionsAlternative alt: _altList){
            if(!alt.saveData()){
                success = false;
                System.out.println("Alternative " + alt.getName() + " could not save");
            }
        }
        return success;
    }
    @Override
    protected InitialConditionsAlternative newAlternative(String string) {
        return new InitialConditionsAlternative(string);
    }
    @Override
    protected NewObjectFactory getAltObjectFactory() {
        return new InitialConditionsAlternativeFactory(this);
    }
    @Override
    public List<DataLocation> getDataLocations(ModelAlternative ma, int i) {
        InitialConditionsAlternative alt = getAlt(ma);
        if(alt==null)return null;
        if(DataLocation.INPUT_LOCATIONS == i){
            //input
            return alt.getInputDataLocations();
        }else{
            //ouput
            return alt.getOutputDataLocations();
        }
    }
    @Override
    public boolean setDataLocations(ModelAlternative ma, List<DataLocation> list) throws ModelLinkingException {
        InitialConditionsAlternative alt = getAlt(ma);
        if(alt!=null){
            return alt.setDataLocations(list);
        }
        return true;
    }
    @Override
    public boolean compute(ModelAlternative ma) {
        InitialConditionsAlternative alt = getAlt(ma);
        if(alt!=null){
            alt.setComputeOptions(ma.getComputeOptions());
            return alt.compute();
        }
        return false;
    }
    @Override
    public void editAlternative(InitialConditionsAlternative e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public boolean displayApplication() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public List<GraphicElement> getGraphicElements(ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public List<OutputElement> getOutputReports(ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public boolean displayEditor(GraphicElement ge) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public boolean displayOutput(OutputElement oe, List<ModelAlternative> list) throws OutputException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public List<EditAction> getEditActions(ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void editAction(String string, ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OutputVariable> getAvailOutputVariables(ModelAlternative ma) {
        List<OutputVariable> ret = new ArrayList<>();
        OutputVariableImpl output = new OutputVariableImpl();
        output.setName(ma._name + " Starting Pool");
        output.setParamId(Parameter.PARAMID_ELEV);
        output.setDescription("Initial Pool Elevation for " + ma._name);
        ret.add(output);
        OutputVariableImpl output2 = new OutputVariableImpl();
        output2.setName(ma._name + " Starting Flow");
        output2.setParamId(Parameter.PARAMID_FLOW);
        output2.setDescription("Initial Flow for " + ma._name);
        ret.add(output2);
        return ret;
    }

    @Override
    public boolean computeOutputVariables(List<OutputVariable> list, ModelAlternative ma) {
        for(OutputVariable o : list){
            OutputVariableImpl oimpl = (OutputVariableImpl)o;
            InitialConditionsAlternative alt = getAlt(ma);
            oimpl.setValue(alt.getOutputValue(oimpl));
        }
        return true;
    }

    @Override
    public boolean hasOutputVariables(ModelAlternative ma) {
        return true;
    }

}
