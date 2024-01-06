package tech.chorume.bot.containers.scanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tech.chorume.bot.core.annotations.CommandBuilder;
import tech.chorume.bot.core.containers.scanner.ClassScanner;
import tech.chorume.bot.core.containers.loader.ComponentLoader;
import tech.chorume.bot.core.containers.scanner.JarClassScanner;
import tech.chorume.bot.core.containers.scanner.LocalPathClassScanner;
import tech.chorume.bot.core.containers.loader.strategies.AnnotationFilter;
import tech.chorume.bot.core.containers.loader.strategies.CompositeFilter;
import tech.chorume.bot.core.containers.loader.strategies.InterfaceFilter;
import tech.chorume.bot.core.interfaces.SlashCommandBuilder;
import tech.chorume.bot.core.interfaces.SlashCommandHandler;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ComponentScannerTest {

    @Test
    @DisplayName("Should find annotated classes in running context")
    public void scanTestAnnotation() {
        try (MockedStatic<ClassScanner> mockedFactory = Mockito.mockStatic(ClassScanner.class)) {
            // static mock main class because we are in testing context
            mockedFactory.when(ClassScanner::getMainClass).thenReturn(ComponentScannerTest.class);
            mockedFactory.when(ClassScanner::getClassScanners).thenReturn(Arrays.asList(new JarClassScanner(), new LocalPathClassScanner()));

            AnnotationFilter annotationFilter = new AnnotationFilter(CommandBuilder.class);
            ComponentLoader scanner = new ComponentLoader(annotationFilter);
            Collection<Class<?>> classes = scanner.scan();
            Assertions.assertEquals(2, classes.size(), "Should find 2 classes annotated with CommandBuilder.class");
            Assertions.assertEquals(1, classes.stream().filter(clazz -> clazz.getName().contains("SlashCommandBuilder1")).count(), "Should find SlashCommandBuilder1 as annotated class");
            Assertions.assertEquals(1, classes.stream().filter(clazz -> clazz.getName().contains("SlashCommandBuilder3NoInterface")).count(), "Should find SlashCommandBuilder3NoInterface as annotated class");
        }
    }

    @Test
    @DisplayName("Should find classes implementing interface in running context")
    public void scanTestInterface() {
        try (MockedStatic<ClassScanner> mockedFactory = Mockito.mockStatic(ClassScanner.class)) {
            // static mock main class because we are in testing context
            mockedFactory.when(ClassScanner::getMainClass).thenReturn(ComponentScannerTest.class);
            mockedFactory.when(ClassScanner::getClassScanners).thenReturn(Arrays.asList(new JarClassScanner(), new LocalPathClassScanner()));

            InterfaceFilter interfaceFilter = new InterfaceFilter(SlashCommandBuilder.class);
            ComponentLoader scanner = new ComponentLoader(interfaceFilter);
            Collection<Class<?>> classes = scanner.scan();
            Assertions.assertEquals(2, classes.size(), "Should find 2 classes implementing SlashCommandBuilder.class interface");
            Assertions.assertEquals(1, classes.stream().filter(clazz -> clazz.getName().contains("SlashCommandBuilder1")).count(), "Should find SlashCommandBuilder1 as an implementing class");
            Assertions.assertEquals(1, classes.stream().filter(clazz -> clazz.getName().contains("SlashCommandBuilder2NoAnnotation")).count(), "Should find SlashCommandBuilder2NoAnnotation as an implementing class");
        }
    }

    @Test
    @DisplayName("Should find bot command builder classes in running context")
    public void scanTestBotCommand() {
        try (MockedStatic<ClassScanner> mockedFactory = Mockito.mockStatic(ClassScanner.class)) {
            // static mock main class because we are in testing context
            mockedFactory.when(ClassScanner::getMainClass).thenReturn(ComponentScannerTest.class);
            mockedFactory.when(ClassScanner::getClassScanners).thenReturn(Arrays.asList(new JarClassScanner(), new LocalPathClassScanner()));

            var filters = List.of(
                    new AnnotationFilter(CommandBuilder.class),
                    new InterfaceFilter(SlashCommandBuilder.class)
            );

            CompositeFilter botCommandFilter = new CompositeFilter(filters);
            ComponentLoader scanner = new ComponentLoader(botCommandFilter);
            Collection<Class<?>> classes = scanner.scan();
            Assertions.assertEquals(1, classes.size(), "Should find 1 bot command builder class in running context");
            Assertions.assertEquals(1, classes.stream().filter(clazz -> clazz.getName().contains("SlashCommandBuilder1")).count(), "Should find SlashCommandBuilder1 as a command builder in running context");
        }
    }

    @Test
    @DisplayName("Should not find anything while scanning running context")
    public void scanTestNoResults() {
        try (MockedStatic<ClassScanner> mockedFactory = Mockito.mockStatic(ClassScanner.class)) {
            // static mock main class because we are in testing context
            mockedFactory.when(ClassScanner::getMainClass).thenReturn(ComponentScannerTest.class);
            mockedFactory.when(ClassScanner::getClassScanners).thenReturn(Arrays.asList(new JarClassScanner(), new LocalPathClassScanner()));

            InterfaceFilter filter1 = new InterfaceFilter(SlashCommandHandler.class);
            ComponentLoader scanner1 = new ComponentLoader(filter1);

            AnnotationFilter filter2 = new AnnotationFilter(Annotation.class);
            ComponentLoader scanner2 = new ComponentLoader(filter2);

            Assertions.assertEquals(0, scanner1.scan().size(), "Should not find any class implementing SlashCommandHandler.class interface in running context");
            Assertions.assertEquals(0, scanner2.scan().size(), "Should not find any class annotated with SlashCommandHandler.class in running context");
        }
    }

}
