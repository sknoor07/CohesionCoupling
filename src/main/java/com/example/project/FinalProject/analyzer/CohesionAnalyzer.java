package com.example.project.FinalProject.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

@Service
public class CohesionAnalyzer {

    public Map<String, Double> analyze(File folder) throws IOException {
        Map<String, Double> cohesionMetrics = new HashMap<>();

        Files.walk(folder.toPath())
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        File file = path.toFile();
                        JavaParser javaParser = new JavaParser();
                        ParseResult<CompilationUnit> parseResult = javaParser.parse(file);

                        java.util.Optional<CompilationUnit> optionalCompilationUnit = parseResult.getResult();
                        if (optionalCompilationUnit.isEmpty()) {
                            throw new IOException("Unable to parse the provided file: " + file.getName());
                        }

                        CompilationUnit compilationUnit = optionalCompilationUnit.get();
                        List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);

                        for (ClassOrInterfaceDeclaration cls : classes) {
                            double cohesion = calculateCohesion(cls);
                            cohesionMetrics.put(cls.getNameAsString(), cohesion);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return cohesionMetrics;
    }

    private double calculateCohesion(ClassOrInterfaceDeclaration cls) {
        List<MethodDeclaration> methods = cls.getMethods();
        int methodCount = methods.size();
        if (methodCount == 0) {
            return 1.0;
        }

        int methodPairs = methodCount * (methodCount - 1) / 2;
        int relatedPairs = 0;

        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                if (areMethodsRelated(methods.get(i), methods.get(j))) {
                    relatedPairs++;
                }
            }
        }

        if (methodPairs == 0) {
            return 1.0;
        }

        return (double) relatedPairs / methodPairs;
    }

    private boolean areMethodsRelated(MethodDeclaration m1, MethodDeclaration m2) {
        boolean shareFieldAccess = m1.findAll(FieldAccessExpr.class).stream()
                .anyMatch(f -> m2.findAll(FieldAccessExpr.class).contains(f));

        boolean callsEachOther = m1.findAll(MethodCallExpr.class).stream()
                .anyMatch(call -> call.getNameAsString().equals(m2.getNameAsString())) ||
                m2.findAll(MethodCallExpr.class).stream()
                .anyMatch(call -> call.getNameAsString().equals(m1.getNameAsString()));

        return shareFieldAccess || callsEachOther;
    }
}
