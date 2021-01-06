/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package initialConditionsPlugin;
import com.rma.client.Browser;
import com.rma.io.DssFileManagerImpl;
import com.rma.io.RmaFile;
import com.rma.model.Project;
import hec.data.Parameter;
import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDSSDataAttributes;
import hec.io.DSSIdentifier;
import hec.io.TimeSeriesContainer;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import hec2.wat.client.WatFrame;
import hec2.wat.model.tracking.OutputVariableImpl;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
/**
 *
 * @author WatPowerUser
 */
public class InitialConditionsAlternative extends SelfContainedPluginAlt{
    private List<DataLocation> _dataLocations = new ArrayList<>();
    private String _pluginVersion;
    private static final String DocumentRoot = "IC";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private ComputeOptions _computeOptions;
    private double _initialPool;
    private double _initialFlow;
    private double _minFlow = 600;
    private String _uFilePath = "/ras/Russian_River_at_Cloverdale.u02";
    public InitialConditionsAlternative(){
        super();
        _dataLocations = new ArrayList<>();
    }
    public InitialConditionsAlternative(String name){
        this();
        setName(name);
    }
    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute,getName());
            root.setAttribute(AlternativeDescriptionAttribute,getDescription());
            if(_dataLocations!=null){
                saveDataLocations(root,_dataLocations);
            }
            Document doc = new Document(root);
            return writeXMLFile(doc,file);
        }
        return false;
    }
    @Override
    protected boolean loadDocument(org.jdom.Document dcmnt) {
        if(dcmnt!=null){
            org.jdom.Element ele = dcmnt.getRootElement();
            if(ele==null){
                System.out.println("No root element on the provided XML document.");
                return false;   
            }
            if(ele.getName().equals(DocumentRoot)){
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));
            }else{
                System.out.println("XML document root was imporoperly named.");
                return false;
            }
            if(_dataLocations==null){
                _dataLocations = new ArrayList<>();
            }
            _dataLocations.clear();
            loadDataLocations(ele, _dataLocations);
            setModified(false);
            return true;
        }else{
            System.out.println("XML document was null.");
            return false;
        }
    }
    public List<DataLocation> getOutputDataLocations(){
       //construct output data locations 
        List<DataLocation> ret = new ArrayList<>();
	return ret;//defaultDataLocations();
    }
    public List<DataLocation> getInputDataLocations(){
        //construct input data locations.
        if(_dataLocations.isEmpty()){
            //return defaultDataLocations();
            return defaultDataLocations();
        }else{
            return _dataLocations;
        }
	
    }
    private List<DataLocation> defaultDataLocations(){
       	if(!_dataLocations.isEmpty()){
            return _dataLocations;
        }
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of intrest for trinity, so that it can be linked to output from other models.
        
        //pool inflows - this is the two locations to link in the Model Linking Editor.
        DataLocation poolElevationLoc = new DataLocation(this.getModelAlt(),"LAKE MENDOCINO-POOL","ELEV");
        dlList.add(poolElevationLoc);
        DataLocation initialFlowLoc = new DataLocation(this.getModelAlt(),"LAKE MENDOCINO-POOL","FLOW-OUT");
        dlList.add(initialFlowLoc);
	return dlList; 
    }
    public boolean setDataLocations(List<DataLocation> dataLocations){
        boolean retval = true;
        for(DataLocation dl : dataLocations){
            if(!_dataLocations.contains(dl)){
                DataLocation linkedTo = dl.getLinkedToLocation();
                if(linkedTo!=null){
                    if(validLinkedToDssPath(dl))
                    {
                        setModified(true);
                        //setDssParts(dl);
                        _dataLocations.add(dl);
                        retval = true;
                    }                    
                }
            }else{
                DataLocation linkedTo = dl.getLinkedToLocation();
                if(linkedTo!=null){
                    if(validLinkedToDssPath(dl))
                    {
                        setModified(true);
                        retval = true;
                    }                    
                }
            }
        }
        if(retval)saveData();
	return retval;
    }
    private boolean validLinkedToDssPath(DataLocation dl){
        DataLocation linkedTo = dl.getLinkedToLocation();
        if(linkedTo==null)return false;
        String dssPath = linkedTo.getDssPath();
        return !(dssPath == null || dssPath.isEmpty());
    }
    public void setComputeOptions(ComputeOptions opts){
        _computeOptions = opts;
    }
    @Override
    public boolean isComputable() {
        return true;
    }
    @Override
    public boolean compute() {
        if(_computeOptions instanceof hec2.wat.model.ComputeOptions){
            boolean returnValue = true;
            hec2.wat.model.ComputeOptions wco = (hec2.wat.model.ComputeOptions)_computeOptions;
            if(!wco.isFrmCompute()){ return false;} 
            //stochastic
            WatFrame fr = null;
            fr = hec2.wat.WAT.getWatFrame();
            //pull linked variables from Ressim then update u file
            // gather the required values from ResSim
            String dssFilePath = wco.getDssFilename();
            for(DataLocation dl : _dataLocations){
            //read input data source
                String dssPath = dl.getLinkedToLocation().getDssPath();
                TimeSeriesContainer tsc = ReadTimeSeries(dssFilePath,dssPath,wco.isFrmCompute());
                if("ELEV".equals(dl.getParameter())){//this was my bug, i had dl.getParameter()=="ELEV" - which is object reference equals, not character equals...
                    //elevation - when comparing strings, always use .equals() unless you want to know they are the same exact memory pointer.
                    _initialPool =  tsc.values[0];
                    //fr.addMessage("Initial pool found as " + _initialPool);
                }else{
                    //not elevation... must be Flow-out...
                    _initialFlow = tsc.values[0];
                    if(_initialFlow < _minFlow) _initialFlow = _minFlow;
                    //fr.addMessage("Parameter is " + dl.getParameter());
                    //fr.addMessage("Initial flow found as " + _initialFlow);
                }
            }
            //write output data to RAS file. 
            Project proj = Browser.getBrowserFrame().getCurrentProject();
            String dir = proj.getProjectDirectory();
             //define RAS ufile path..
            String uFile = dir + _uFilePath;
            RAS_UNSTEADY_READER r = new RAS_UNSTEADY_READER(uFile,_initialPool,_initialFlow);
            returnValue = r.updateFile();
            return returnValue;
        }
        return false;
    }
    private TimeSeriesContainer ReadTimeSeries(String DssFilePath, String dssPath, boolean isFRM){
        DSSPathname pathName = new DSSPathname(dssPath);
        String InputFPart = pathName.getFPart();
        if(isFRM){
            int AltFLastIdx = _computeOptions.getFpart().lastIndexOf(":");
            if(InputFPart.contains(":")){
                int oldFLastIdx = InputFPart.lastIndexOf(":");
                pathName.setFPart(_computeOptions.getFpart().substring(0,AltFLastIdx)+ InputFPart.substring(oldFLastIdx,InputFPart.length()));
            }  
        }
        DSSIdentifier eventDss = new DSSIdentifier(DssFilePath,pathName.getPathname());
        eventDss.setStartTime(_computeOptions.getRunTimeWindow().getStartTime());
	eventDss.setEndTime(_computeOptions.getRunTimeWindow().getEndTime());
        int type = DssFileManagerImpl.getDssFileManager().getRecordType(eventDss);
        if((HecDSSDataAttributes.REGULAR_TIME_SERIES<=type && type < HecDSSDataAttributes.PAIRED)){
            boolean exist = DssFileManagerImpl.getDssFileManager().exists(eventDss);
            TimeSeriesContainer eventTsc = null;
            if (!exist )
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            eventTsc = DssFileManagerImpl.getDssFileManager().readTS(eventDss, true);
            if ( eventTsc != null )
            {
                exist = eventTsc.numberValues > 0;
            }
            if(exist){
                return eventTsc;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    protected double getOutputValue(OutputVariableImpl oimpl){
        if(oimpl.getParamId()==Parameter.PARAMID_FLOW){
            return _initialFlow;
        }else{
            return _initialPool;
        }
    }
    @Override
    public boolean cancelCompute() {
        return false;
    }
    @Override
    public String getLogFile() {
        return null;
    }
    @Override
    public int getModelCount() {
        return 1;
    }

}
