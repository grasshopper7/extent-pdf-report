package tech.grasshopper.pdf.extent;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aventstack.extentreports.model.Report;
import com.aventstack.extentreports.observer.ReportObserver;
import com.aventstack.extentreports.observer.entity.ReportEntity;
import com.aventstack.extentreports.reporter.AbstractFileReporter;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import tech.grasshopper.pdf.PDFCucumberReport;
import tech.grasshopper.pdf.data.ReportData;

public class ExtentPDFCucumberReporter extends AbstractFileReporter
implements ReportObserver<ReportEntity> {
	private static final Logger logger = Logger.getLogger(ExtentPDFCucumberReporter.class.getName());
	private static final String REPORTER_NAME = "pdf";
	private static final String FILE_NAME = "Index.pdf";

	private Disposable disposable;
	private Report report;

	public ExtentPDFCucumberReporter(String path) {
		super(new File(path));
	}

	public ExtentPDFCucumberReporter(File f) {
		super(f);
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
			report = value.getReport();
			final String filePath = getFileNameAsExt(FILE_NAME, new String[]{".pdf"});
			
			ExtentPDFReportDataGenerator generator = new ExtentPDFReportDataGenerator();
			ReportData reportData = generator.generateReportData(report);

			PDFCucumberReport pdfCucumberReport = new PDFCucumberReport(reportData, new File(filePath));
			pdfCucumberReport.createReport();
		} catch (Exception e) {
			disposable.dispose();
			logger.log(Level.SEVERE, "An exception occurred", e);
		}
	}
}
