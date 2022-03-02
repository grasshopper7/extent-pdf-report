package tech.grasshopper.pdf.extent.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.aventstack.extentreports.model.Log;
import com.aventstack.extentreports.model.ScreenCapture;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class MediaProcessor extends Processor {

	private static final AtomicInteger EMBEDDED_INT = new AtomicInteger(0);

	public static final String EMBEDDED_PREFIX = "base64_generated_pdf_";

	@NonNull
	private String mediaFolder;

	public List<String> process() {

		List<String> mediaPaths = new ArrayList<>();

		for (Log log : logs) {
			if (log.getStatus() == com.aventstack.extentreports.Status.INFO && log.getMedia() != null) {
				ScreenCapture media = (ScreenCapture) log.getMedia();
				String path = "";

				if (media.getPath() != null && !media.getPath().isEmpty()) {
					path = mediaFolder + "/" + Paths.get(log.getMedia().getPath()).getFileName().toString();

					if (!Paths.get(path).toFile().exists())
						path = createNoImageFoundFileStructure().toString();

				} else if (media.getBase64() != null && !media.getBase64().isEmpty()) {
					// Generate physical file as pdfbox currently does not support base64 string
					// image generation
					Path mediaFile = createMediaFileStructure();

					try {
						// Remove the prefix added by extent report 'data:image/png;base64,'
						Files.write(mediaFile, Base64.getDecoder().decode(media.getBase64().substring(22)));
						path = mediaFile.toString();
					} catch (IOException e) {
						path = createNoImageFoundFileStructure().toString();
					}

				} else
					path = createNoImageFoundFileStructure().toString();

				mediaPaths.add(path);
			}
		}

		return mediaPaths;
	}

	private Path createMediaFileStructure() {
		StringBuilder fileName = new StringBuilder(EMBEDDED_PREFIX).append(EMBEDDED_INT.incrementAndGet()).append(".")
				.append("png");

		File dir = new File(mediaFolder);
		// Create directory if not existing
		if (!dir.exists())
			dir.mkdirs();

		Path path = Paths.get(mediaFolder, fileName.toString());
		return path;
	}

	@SneakyThrows
	private Path createNoImageFoundFileStructure() {
		Path path = Paths.get(mediaFolder, "not-found-image.png");

		if (path.toFile().exists())
			return path;

		File dir = new File(mediaFolder);
		// Create directory if not existing
		if (!dir.exists())
			dir.mkdirs();

		Files.write(path, Base64.getDecoder().decode(NoImageFile.BASE64_STR));
		return path;
	}
}
