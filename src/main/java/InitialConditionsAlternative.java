/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.client.Browser;
import com.rma.io.DssFileManagerImpl;
import com.rma.io.RmaFile;
import com.rma.model.Project;
import hec.data.Parameter;
import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDSSDataAttributes;
import hec.heclib.util.HecTime;
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
    private RASReservoir[] _reservoirs;
    private String _uFilePath = "/ras/Trinity_WAT.u06";
    public InitialConditionsAlternative(){
        super();
        _dataLocations = new ArrayList<>();
    }
    public InitialConditionsAlternative(String name){
        this();
        setName(name);
        buildDefaultReservoirs();
    }
    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            //used to be sElement
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
                System.out.println("XML document root was improperly named.");
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
        //List<DataLocation> ret = new ArrayList<>();
	return defaultDataLocations();
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
    private void buildDefaultReservoirs(){
        _reservoirs = new RASReservoir[6];
        _reservoirs[0] = new RASReservoir(new RASName("Clear_Fork      ","Clear_Fork      ", "60.4808 ","60.4751 ", "Benbrook Lake   "), "BENBROOK-POOL", 600 );
        _reservoirs[1] = new RASReservoir(new RASName("Denton_Creek    ","DC              ", "11.5    ","11.48   ", "Grapevine       "), "GRAPEVINE-POOL", 600 );
        _reservoirs[2] = new RASReservoir(new RASName("Elm Fork        ","Upper           ", "59.0    ","58.9    ", "Ray Roberts Lake"), "RAY ROBERTS-POOL", 600 );
        _reservoirs[3] = new RASReservoir(new RASName("Mountain_Creek  ","Joe_Pool        ", "12.038  ","12.034  ", "Joe_Pool        "), "JOE POOL-POOL", 600 );
        _reservoirs[4] = new RASReservoir(new RASName("Elm Fork        ","Upper           ", "30.00   ","30.100  ", "Lewisville Lake "), "LEWISVILLE-POOL", 600 );
        _reservoirs[5] = new RASReservoir(new RASName("Mountain_Creek  ","Joe_Pool        ", "4.267   ","4.284   ", "Mountain_Creek  "), "MOUNTAIN CREEK-POOL", 600 );
    }
    private List<DataLocation> defaultDataLocations(){
       	if(!_dataLocations.isEmpty()){
            return _dataLocations;
        }
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of intrest for trinity, so that it can be linked to output from other models.
        //pool inflows - this is the two locations to link in the Model Linking Editor.
        for(RASReservoir r: _reservoirs){
            dlList.add(new DataLocation(this.getModelAlt(),r.get_ResSimName(),"ELEV"));
            dlList.add(new DataLocation(this.getModelAlt(),r.get_ResSimName(),"FLOW-OUT"));
        }

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
            hec.model.RunTimeWindow rtw = wco.getRunTimeWindow(); //USe this to identfiy range to pull from timeseries.
            HecTime firstTimestep = rtw.getStartTime();

            //stochastic
            WatFrame fr = null;
            fr = hec2.wat.WAT.getWatFrame();
            //pull linked variables from Ressim then update u file
            // gather the required values from ResSim
            String dssFilePath = wco.getDssFilename();
            for(DataLocation dl : _dataLocations){
                RASReservoir resPointer = null;
                for(RASReservoir r: _reservoirs){
                    if(r.get_ResSimName().equals(dl.getName())){
                        resPointer = r;
                    }
                }
            //read input data source
                String dssPath = dl.getLinkedToLocation().getDssPath();
                TimeSeriesContainer tsc = ReadTimeSeries(dssFilePath,dssPath,wco.isFrmCompute());
                if("ELEV".equals(dl.getParameter())){//this was my bug, i had dl.getParameter()=="ELEV" - which is object reference equals, not character equals...
                    //elevation - when comparing strings, always use .equals() unless you want to know they are the same exact memory pointer.
                    double myInitialPool = tsc.getValue(firstTimestep);
                    while(myInitialPool < 0){
                        firstTimestep.addHours(1);
                        myInitialPool = tsc.getValue(firstTimestep);
                    }
                    resPointer.set_initialPool(myInitialPool); // We need to check this reflects the TWM *************

                }else{
                    //not elevation... must be Flow-out...
                    double initialFlow = tsc.values[0];
                    while(initialFlow < 0){
                        firstTimestep.addHours(1);
                        initialFlow = tsc.getValue(firstTimestep);
                    }
                    if(initialFlow < resPointer.get_minFlow()) initialFlow = resPointer.get_minFlow();
                    resPointer.set_initialFlow(initialFlow);

                }
            }
            //write output data to RAS file. 
            Project proj = Browser.getBrowserFrame().getCurrentProject();
            String dir = proj.getProjectDirectory();
             //define RAS ufile path..
            String uFile = dir + _uFilePath;

            RAS_UNSTEADY_READER r = new RAS_UNSTEADY_READER(uFile,uFile,_reservoirs);
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
