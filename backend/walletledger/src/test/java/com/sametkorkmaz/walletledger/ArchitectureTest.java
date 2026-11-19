package com.sametkorkmaz.walletledger;

import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
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

    @ArchTest
    static final ArchRule entriesAreBuiltByLedgerEntryFactory = noClasses()
            .that().doNotBelongToAnyOf(LedgerEntry.class)
            .and().resideOutsideOfPackage("..persistence..")
            .should().callConstructorWhere(target(owner(type(LedgerEntry.class))))
            .because("entries are built by LedgerEntry factories; only the store rebuilds loaded rows");
}
