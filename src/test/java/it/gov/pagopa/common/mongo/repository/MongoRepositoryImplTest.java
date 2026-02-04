package it.gov.pagopa.common.mongo.repository;

import it.gov.pagopa.iban.model.IbanModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoRepositoryImplTest {

    @Mock
    MongoOperations mongoOperations;

    @Mock
    MongoEntityInformation<IbanModel, String> entityInformation;

    MongoRepositoryImpl<IbanModel, String> repository;

    @BeforeEach
    void setUp() {
        repository = new MongoRepositoryImpl<>(entityInformation, mongoOperations);

        when(entityInformation.getJavaType()).thenReturn(IbanModel.class);
        when(entityInformation.getCollectionName()).thenReturn("testCollection");
    }

    @Test
    void findById_shouldReturnEntity_whenFound() {

        IbanModel entity = new IbanModel();
        entity.setId("ID");

        when(mongoOperations.find(any(), any(), any()))
                .thenReturn(List.of(entity));

        Optional<IbanModel> result = repository.findById("ID");

        assertTrue(result.isPresent());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {

        when(mongoOperations.find(any(), any(), any()))
                .thenReturn(List.of());

        Optional<IbanModel> result = repository.findById("ID");

        assertTrue(result.isEmpty());
    }
}
