package ru.kai.mcard;

import org.junit.Before;
import org.junit.Test;

import ru.kai.mcard.data.datasource.DBMethodsS;
import ru.kai.mcard.data.repository.SpecializationRepositoryImpl;
import ru.kai.mcard.domain.interactor.interactorSpecializations.UseCaseSpecializationsGetList;
import ru.kai.mcard.domain.repository.ISpecializationRepository;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by akabanov on 24.10.2017.
 */

public class UseCaseSpecializationsGetListTest {
    private UseCaseSpecializationsGetList useCaseSpecializationsGetList;

    @Before
    public void setUp() throws Exception{

/*
        DBMethodsS dbMethodsS = new DBMethodsS();
        SpecializationRepositoryImpl repository = new SpecializationRepositoryImpl();
        useCaseSpecializationsGetList = new UseCaseSpecializationsGetList(repository);
*/
    }


    @Test
    public void testBuildUseCaseObservable() throws Exception{
//        assertNotNull(useCaseSpecializationsGetList.buildUseCaseFlowable(null));
    }
}
