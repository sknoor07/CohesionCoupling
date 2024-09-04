package com.example.project.FinalProject.controller;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.project.FinalProject.analyzer.CohesionAnalyzer;
import com.example.project.FinalProject.analyzer.CouplingAnalyzer;

@Controller
public class CodeAnalyzerController {

    private final CohesionAnalyzer cohesionAnalyzer= new CohesionAnalyzer();
    private final CouplingAnalyzer couplingAnalyzer = new CouplingAnalyzer();

    @PostMapping("/analyze")
    public String analyze(@RequestParam("files") MultipartFile[] files, Model model) throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "java_files");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        for (File file : tempDir.listFiles()) {
            file.delete();
        }

        for (MultipartFile file : files) {
            File destinationFile = new File(tempDir, file.getOriginalFilename());
            try {
                file.transferTo(destinationFile);
                System.out.println("Saved file: " + file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error saving file: " + file.getOriginalFilename(), e);
            }
        }

        DefaultCategoryDataset cohesionDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset couplingDataset = new DefaultCategoryDataset();

        Map<String, Double> cohesionMetrics = cohesionAnalyzer.analyze(tempDir);
        Map<String, Double> couplingMetrics = couplingAnalyzer.analyze(tempDir);

        cohesionMetrics.forEach((className, cohesion) -> {
            // System.out.println("Cohesion for class: " + className + " = " + cohesion);
            cohesionDataset.addValue(cohesion, "Cohesion", className);
        });

        couplingMetrics.forEach((className, coupling) -> {
            // System.out.println("Coupling for class: " + className + " = " + coupling);
            couplingDataset.addValue(coupling, "Coupling", className);
        });

        File cohesionChartFile = new File("src/main/resources/static/cohesion_chart.png");
        File couplingChartFile = new File("src/main/resources/static/coupling_chart.png");

        saveChart(createBarChart("Cohesion Analysis", "Class Name", "Cohesion", cohesionDataset), cohesionChartFile);
        saveChart(createBarChart("Coupling Analysis", "Class Name", "Coupling", couplingDataset), couplingChartFile);

        model.addAttribute("cohesionChart", "/cohesion_chart.png");
        model.addAttribute("couplingChart", "/coupling_chart.png");

        return "index";
    }

    private void saveChart(JFreeChart chart, File file) {
        try {
            ChartUtils.saveChartAsPNG(file, chart, 800, 600);
            System.out.println("Chart saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving chart: " + e.getMessage());
        }
    }

    private JFreeChart createBarChart(String title, String categoryAxisLabel, String valueAxisLabel, DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);

        return chart;
    }

    @GetMapping("/cohesion_chart.png")
    @ResponseBody
    public ResponseEntity<FileSystemResource> getCohesionChart() {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(new File("src/main/resources/static/cohesion_chart.png")));
    }

    @GetMapping("/coupling_chart.png")
    @ResponseBody
    public ResponseEntity<FileSystemResource> getCouplingChart() {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(new File("src/main/resources/static/coupling_chart.png")));
    }
}

