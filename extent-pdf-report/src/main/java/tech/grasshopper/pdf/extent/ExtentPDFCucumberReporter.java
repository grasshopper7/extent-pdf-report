package tech.grasshopper.pdf.extent;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aventstack.extentreports.model.Report;
import com.aventstack.extentreports.observer.ReportObserver;
import com.aventstack.extentreports.observer.entity.ReportEntity;
import com.aventstack.extentreports.reporter.AbstractFileReporter;
import com.aventstack.extentreports.reporter.ReporterFilterable;
import com.aventstack.extentreports.reporter.configuration.EntityFilters;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.AccessLevel;
import lombok.Getter;
import tech.grasshopper.pdf.PDFCucumberReport;
import tech.grasshopper.pdf.data.ReportData;
import tech.grasshopper.pdf.section.details.executable.MediaCleanup.CleanupType;
import tech.grasshopper.pdf.section.details.executable.MediaCleanup.MediaCleanupOption;

public class ExtentPDFCucumberReporter extends AbstractFileReporter
		implements ReportObserver<ReportEntity>, ReporterFilterable<ExtentPDFCucumberReporter> {

	private static final Logger logger = Logger.getLogger(ExtentPDFCucumberReporter.class.getName());
	private static final String REPORTER_NAME = "pdf";
	private static final String FILE_NAME = "Index.pdf";

	private Disposable disposable;
	private Report report;
	private String mediaFolder;
	private MediaCleanupOption mediaCleanupOption;

	@Getter(value = AccessLevel.NONE)
	private final EntityFilters<ExtentPDFCucumberReporter> filter = new EntityFilters<>(this);

	public ExtentPDFCucumberReporter(String path, String mediaFolder) {
		this(new File(path), mediaFolder, MediaCleanupOption.builder().cleanUpType(CleanupType.NONE).build());
	}

	public ExtentPDFCucumberReporter(File f, String mediaFolder) {
		this(f, mediaFolder, MediaCleanupOption.builder().cleanUpType(CleanupType.NONE).build());
	}

	public ExtentPDFCucumberReporter(String path, String mediaFolder, MediaCleanupOption mediaCleanupOption) {
		this(new File(path), mediaFolder, mediaCleanupOption);
	}

	public ExtentPDFCucumberReporter(File f, String mediaFolder, MediaCleanupOption mediaCleanupOption) {
		super(f);
		this.mediaFolder = mediaFolder;
		this.mediaCleanupOption = mediaCleanupOption;
	}

	@Override
	public EntityFilters<ExtentPDFCucumberReporter> filter() {
		return filter;
	}

	public Observer<ReportEntity> getReportObserver() {
		return new Observer<ReportEntity>() {

			public void onSubscribe(Disposable d) {
				start(d);
			}

			public void onNext(ReportEntity value) {
				flush(value);
			}

			public void onError(Throwable e) {
			}

			public void onComplete() {
			}
		};
	}

	private void start(Disposable d) {
		disposable = d;
	}

	private void flush(ReportEntity value) {
		try {
			report = filterAndGet(value.getReport(), filter.statusFilter().getStatus());
			final String filePath = getFileNameAsExt(FILE_NAME, new String[] { ".pdf" });

			ExtentPDFReportDataGenerator generator = ExtentPDFReportDataGenerator.builder().mediaFolder(mediaFolder)
					.build();
			ReportData reportData = generator.generateReportData(report);

			PDFCucumberReport pdfCucumberReport = new PDFCucumberReport(reportData, new File(filePath),
					mediaCleanupOption);
			pdfCucumberReport.createReport();
		} catch (Exception e) {
			disposable.dispose();
			logger.log(Level.SEVERE, "An exception occurred", e);
		}
	}
}
