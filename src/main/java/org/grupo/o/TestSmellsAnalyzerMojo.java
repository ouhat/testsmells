package org.grupo.o;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY)
public class TestSmellsAnalyzerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/test/java", property = "testDirectory", required = true)
    private File testDirectory;

    public void execute() throws MojoExecutionException {
        getLog().info("Iniciando análisis de Test Smells...");

        try (Stream<Path> paths = Files.walk(testDirectory.toPath())) {
            List<Path> testFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            for (Path filePath : testFiles) {
                analyzeFile(filePath);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error al analizar el directorio de tests", e);
        }

        getLog().info("Análisis de Test Smells completado.");
    }

    private void analyzeFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);

        if (detectAssertionRoulette(lines)) getLog().warn("Assertion Roulette detectado en: " + filePath);
        if (detectConditionalTestLogic(lines)) getLog().warn("Conditional Test Logic detectado en: " + filePath);
        if (detectConstructorInitialization(lines)) getLog().warn("Constructor Initialization detectado en: " + filePath);
        if (detectDefaultTest(lines, filePath)) getLog().warn("Default Test detectado en: " + filePath);
        if (detectDuplicateAssert(lines)) getLog().warn("Duplicate Assert detectado en: " + filePath);
        if (detectEagerTest(lines)) getLog().warn("Eager Test detectado en: " + filePath);
        if (detectEmptyTest(lines)) getLog().warn("Empty Test detectado en: " + filePath);
        if (detectExceptionHandling(lines)) getLog().warn("Exception Handling detectado en: " + filePath);
        if (detectGeneralFixture(lines)) getLog().warn("General Fixture detectado en: " + filePath);
        if (detectIgnoredTest(lines)) getLog().warn("Ignored Test detectado en: " + filePath);
        if (detectLazyTest(lines)) getLog().warn("Lazy Test detectado en: " + filePath);
        if (detectMagicNumberTest(lines)) getLog().warn("Magic Number Test detectado en: " + filePath);
        if (detectMysteryGuest(lines)) getLog().warn("Mystery Guest detectado en: " + filePath);
        if (detectRedundantPrint(lines)) getLog().warn("Redundant Print detectado en: " + filePath);
        if (detectRedundantAssertion(lines)) getLog().warn("Redundant Assertion detectado en: " + filePath);
        if (detectResourceOptimism(lines)) getLog().warn("Resource Optimism detectado en: " + filePath);
        if (detectSensitiveEquality(lines)) getLog().warn("Sensitive Equality detectado en: " + filePath);
        if (detectSleepyTest(lines)) getLog().warn("Sleepy Test detectado en: " + filePath);
        if (detectUnknownTest(lines)) getLog().warn("Unknown Test detectado en: " + filePath);
    }

    // Detectores para cada test smell

    private boolean detectAssertionRoulette(List<String> lines) {
        long assertionCount = lines.stream().filter(line -> line.contains("assert") && !line.contains(",")).count();
        return assertionCount > 1;
    }

    private boolean detectConditionalTestLogic(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("if") || line.contains("switch") || line.contains("for") || line.contains("while"));
    }

    private boolean detectConstructorInitialization(List<String> lines) {
        return lines.stream().anyMatch(line -> line.matches(".*public.*\\(.*\\).*\\{.*"));
    }

    private boolean detectDefaultTest(List<String> lines, Path filePath) {
        String fileName = filePath.getFileName().toString();
        return fileName.equals("ExampleUnitTest.java") || fileName.equals("ExampleInstrumentedTest.java");
    }

    private boolean detectDuplicateAssert(List<String> lines) {
        return lines.stream().filter(line -> line.contains("assert")).collect(Collectors.toSet()).size() < lines.stream().filter(line -> line.contains("assert")).count();
    }

    private boolean detectEagerTest(List<String> lines) {
        return lines.stream().filter(line -> line.contains("assert")).count() > 1 && lines.stream().anyMatch(line -> line.matches(".*\\..*\\(.*\\);"));
    }

    private boolean detectEmptyTest(List<String> lines) {
        return lines.stream().allMatch(line -> line.trim().isEmpty() || line.trim().startsWith("//"));
    }

    private boolean detectExceptionHandling(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("throw") || line.contains("catch"));
    }

    private boolean detectGeneralFixture(List<String> lines) {
        boolean hasSetUp = lines.stream().anyMatch(line -> line.contains("void setUp"));
        boolean hasUnusedFields = lines.stream().anyMatch(line -> line.matches("private .*;") && !line.contains("assert"));
        return hasSetUp && hasUnusedFields;
    }

    private boolean detectIgnoredTest(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("@Ignore"));
    }

    private boolean detectLazyTest(List<String> lines) {
        return lines.stream().filter(line -> line.contains("assert")).count() > 1 && lines.stream().anyMatch(line -> line.matches(".*\\..*\\(.*\\);"));
    }

    private boolean detectMagicNumberTest(List<String> lines) {
        return lines.stream().anyMatch(line -> line.matches(".*assert.*\\(.*\\d+.*\\);"));
    }

    private boolean detectMysteryGuest(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("File") || line.contains("Database"));
    }

    private boolean detectRedundantPrint(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("System.out.print"));
    }

    private boolean detectRedundantAssertion(List<String> lines) {
        return lines.stream().anyMatch(line -> line.matches(".*assertEquals\\(.*true, true.*\\);"));
    }

    private boolean detectResourceOptimism(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("File") && !(line.contains("exists()") || line.contains("isFile()")));
    }

    private boolean detectSensitiveEquality(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains(".toString()") && line.contains("assert"));
    }

    private boolean detectSleepyTest(List<String> lines) {
        return lines.stream().anyMatch(line -> line.contains("Thread.sleep"));
    }

    private boolean detectUnknownTest(List<String> lines) {
        return lines.stream().noneMatch(line -> line.contains("assert"));
    }
}
