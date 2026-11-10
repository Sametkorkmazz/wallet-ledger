package com.sametkorkmaz.walletledger.ledger;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.sametkorkmaz.walletledger", importOptions = ImportOption.DoNotIncludeTests.class)
class LedgerArchitectureTest {

    @ArchTest
    static final ArchRule keepsEntitiesInsidePersistencePackage = noClasses()
            .that()
            .resideOutsideOfPackage("..ledger.persistence..")
            .should()
            .dependOnClassesThat(JavaClass.Predicates.resideInAPackage("..ledger.persistence..")
                    .and(JavaClass.Predicates.simpleNameEndingWith("Entity")))
            .because("entities are an implementation detail of ledger.persistence");
}
