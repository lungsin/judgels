package org.iatoki.judgels.sandalphon.problem.base;

import com.google.common.collect.ImmutableList;
import judgels.persistence.hibernate.HibernateDaoData;
import judgels.persistence.hibernate.JudgelsHibernateDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class ProblemHibernateDao extends JudgelsHibernateDao<ProblemModel> implements ProblemDao {

    @Inject
    public ProblemHibernateDao(HibernateDaoData data) {
        super(data);
    }

    @Override
    public List<String> getJidsByAuthorJid(String authorJid) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ProblemModel> root = query.from(getEntityClass());

        query
                .select(root.get(ProblemModel_.jid))
                .where(cb.equal(root.get(ProblemModel_.createdBy), authorJid));

        return currentSession().createQuery(query).getResultList();
    }

    @Override
    public ProblemModel findBySlug(String slug) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<ProblemModel> query = cb.createQuery(ProblemModel.class);
        Root<ProblemModel> root = query.from(getEntityClass());

        query.where(cb.equal(root.get(ProblemModel_.slug), slug));

        return currentSession().createQuery(query).getSingleResult();
    }

    @Override
    public boolean existsBySlug(String slug) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProblemModel> root = query.from(getEntityClass());

        query
                .select(cb.count(root))
                .where(cb.equal(root.get(ProblemModel_.slug), slug));

        return currentSession().createQuery(query).getSingleResult() > 0;
    }

    @Override
    protected List<SingularAttribute<ProblemModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ProblemModel_.slug, ProblemModel_.additionalNote);
    }
}
