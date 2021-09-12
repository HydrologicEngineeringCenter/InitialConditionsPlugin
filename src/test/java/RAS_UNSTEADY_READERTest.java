import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.io.FileUtils;

class RAS_UNSTEADY_READERTest {
    private RASReservoir[] _reservoirs;
    @Test
    void updateFileReturnsDesiredFile() {
        _reservoirs = new RASReservoir[6];
        _reservoirs[0] = new RASReservoir(new RASName("Clear_Fork      ","Clear_Fork      ", "60.4808 ","60.4751 ", "Benbrook Lake   "), "BENBROOK-POOL", 600 );
        _reservoirs[1] = new RASReservoir(new RASName("Denton_Creek    ","DC              ", "11.5    ","11.48   ", "Grapevine       "), "GRAPEVINE-POOL", 600 );
        _reservoirs[2] = new RASReservoir(new RASName("Elm Fork        ","Upper           ", "59      ","58.9    ", "Ray Roberts Lake"), "RAY ROBERTS-POOL", 600 );
        _reservoirs[3] = new RASReservoir(new RASName("Mountain_Creek  ","Joe_Pool        ", "12.038  ","12.034  ", "Joe_Pool        "), "JOE POOL-POOL", 600 );
        _reservoirs[4] = new RASReservoir(new RASName("Elm Fork        ","Upper           ", "29.880  ","30.100  ", "Lewisville Lake "), "LEWISVILLE-POOL", 600 );
        _reservoirs[5] = new RASReservoir(new RASName("Mountain_Creek  ","Joe_Pool        ", "4.237   ","4.284   ", "Mountain_Creek  "), "MOUNTAIN CREEK-POOL", 600 );

        for(int i = 0; i<6; i++){
            _reservoirs[i].set_initialFlow(666);
            _reservoirs[i].set_initialPool(104);
        }
        String resourceDirectory = Paths.get("src","test","resources").toString();
        String workingUFile = resourceDirectory+"\\RayRoberts.u03";
        String originalUFile = resourceDirectory+"\\RayRobertsOriginal.u03";
        RAS_UNSTEADY_READER reader = new RAS_UNSTEADY_READER(originalUFile, workingUFile, _reservoirs);

        reader.updateFile();

        File workingfile = new File(resourceDirectory+"\\RayRoberts.u03");
        File targetfile = new File(resourceDirectory+"\\RayRobertsTarget.u03");

        try {
           assertTrue( FileUtils.contentEquals(workingfile, targetfile));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}