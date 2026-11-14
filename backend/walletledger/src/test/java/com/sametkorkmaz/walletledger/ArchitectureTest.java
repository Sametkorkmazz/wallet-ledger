package com.sametkorkmaz.walletledger;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.sametkorkmaz.walletledger", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule nothingOutsidePersistenceDependsOnEntities = noClasses()
            .that()
            .resideOutsideOfPackage("..persistence..")
            .should()
            .dependOnClassesThat()
            .areAnnotatedWith(Entity.class)
            .because("entities are an implementation detail of a persistence package");

    @ArchTest
    static final ArchRule entitiesResideInPersistencePackage = classes()
            .that()
            .areAnnotatedWith(Entity.class)
            .should()
            .resideInAPackage("..persistence..")
            .because("the package-private boundary only protects entities inside a persistence package");
}
