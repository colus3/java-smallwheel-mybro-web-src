package smallwheel.mybro.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import smallwheel.mybro.common.Constants.Path;
import smallwheel.mybro.domain.DbConnectionInfo;
import smallwheel.mybro.domain.GeneratingInfo;
import smallwheel.mybro.service.DatabaseService;

@Controller
public class MainController {
    
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
	
    @Autowired
    DatabaseService databaseService;

    @RequestMapping(value = "/")
    String list(Model model) {
        return "main";
    }
    
    @RequestMapping(value = "/api/connect")
    @ResponseBody ResponseEntity<Void> connect(HttpSession session, @RequestBody DbConnectionInfo connectionInfo) {
    	LOGGER.info("Connect to server at {}:{}", connectionInfo.getHost(), connectionInfo.getPort());
    	databaseService.createDataSource(session.getId(), connectionInfo);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }
    
    @RequestMapping(value = "/api/tables")
    @ResponseBody List<String> getTables(HttpSession session, @RequestBody DbConnectionInfo connectionInfo) {
    	LOGGER.info("Get tables from {}:{}", connectionInfo.getHost(), connectionInfo.getPort());
    	return databaseService.getTables(session.getId(), connectionInfo.getDbmsType());
    }
    
    @RequestMapping(value = "/api/tables/generate", method = RequestMethod.POST)
    @ResponseBody List<String> generate(HttpSession session, @RequestBody GeneratingInfo generatingInfo) {
    	LOGGER.info("Generate {} file(s)...", generatingInfo.getTables().size());
    	databaseService.generate(session.getId(), generatingInfo);
    	return null;
    }
    
    @RequestMapping(value = "/api/tables/zip", method = RequestMethod.GET, produces="application/zip")
	public @ResponseBody byte[] zipFiles(HttpSession session, HttpServletResponse response) throws IOException {
    	
		// setting headers
		response.setContentType("application/zip");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + session.getId() + ".zip\"");

		// creating byteArray stream, make it bufforable and passing this buffor to ZipOutputStream
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
		ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

		// file list
		List<File> files = new ArrayList<File>();
		Files.walk(Paths.get(Path.RESULT_DES_DIR + session.getId())).filter(Files::isRegularFile).forEach(i -> files.add(i.toFile()));

		for (File file : files) {
			// new zip entry and copying inputstream with file to zipOutputStream, after all closing streams
			zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
			FileInputStream fileInputStream = new FileInputStream(file);

			IOUtils.copy(fileInputStream, zipOutputStream);

			fileInputStream.close();
			zipOutputStream.closeEntry();
		}
        
		if (zipOutputStream != null) {
			zipOutputStream.finish();
			zipOutputStream.flush();
			IOUtils.closeQuietly(zipOutputStream);
		}
		
		IOUtils.closeQuietly(bufferedOutputStream);
		IOUtils.closeQuietly(byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}
    
}
