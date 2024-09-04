package com.example.project.FinalProject.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

public class CouplingAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(CouplingAnalyzer.class.getName());
    private final JavaParser javaParser = new JavaParser();

    public Map<String, Double> analyze(File directory) throws IOException {
        Map<String, Double> couplingMap = new HashMap<>();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".java"));
        if (files != null) {
            for (File file : files) {
                try {
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(file);
                    if (parseResult.getResult().isPresent()) {
                        CompilationUnit cu = parseResult.getResult().get();
                        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                            String className = classDecl.getNameAsString();
                            Set<String> externalClasses = new HashSet<>();
                            classDecl.findAll(ObjectCreationExpr.class).forEach(objectCreationExpr -> {
                                String externalClassName = objectCreationExpr.getType().asString();
                                if (!externalClassName.equals(className)) {
                                    externalClasses.add(externalClassName);
                                }
                            });
                            double couplingValue = externalClasses.size();
                            couplingMap.put(className, couplingValue);
                            LOGGER.log(Level.INFO, "Class: " + className + " Coupling Value: " + couplingValue);
                        });
                    } else {
                        LOGGER.log(Level.SEVERE, "Failed to parse file: " + file.getName());
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "I/O error in file: " + file.getName(), e);
                }
            }
        }
        return couplingMap;
    }
}

