package de.bonndan.nivio.man;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@RestController
@RequestMapping(path = ManController.PATH)
public class ManController {

    public static final String PATH = "/man";

    public ManController() {
    }

    /**
     * Overview on all available man pages.
     */
    @CrossOrigin(methods = RequestMethod.GET)
    @RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String[]> index() {
        File file = new File("docs/build");
        String[] pathArr = file.list();
        if (pathArr != null) {
            return new ResponseEntity<>(
                    Arrays.stream(pathArr)
                            .filter(path ->
                                    path.endsWith(".html")
                                    && !path.equals("search.html")
                                    && !path.equals("genindex.html")
                            )
                            .toArray(String[]::new),
                    HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * This resource serves a man page and can be addressed by using a fileName
     */
    @CrossOrigin(methods = RequestMethod.GET)
    @RequestMapping(method = RequestMethod.GET, path = "/{file}")
    public ResponseEntity<String> getFileContent(@PathVariable(name = "file") String fileName) {
        if (!fileName.endsWith(".html")) {
            fileName = fileName + ".html";
        }
        File file = new File("docs/build/" + fileName);
        if (file.canRead()) {
            try {
                return new ResponseEntity<>(
                        Files.readString(Paths.get(file.getAbsolutePath()), Charset.defaultCharset()),
                        HttpStatus.OK
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }
}
