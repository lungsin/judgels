package judgels.uriel.contest.problem;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import judgels.uriel.api.contest.problem.ContestProblem;
import judgels.uriel.api.contest.problem.ContestProblemStatus;
import judgels.uriel.persistence.ContestProblemDao;
import judgels.uriel.persistence.ContestProblemModel;

public class ContestProblemStore {
    private final ContestProblemDao problemDao;

    @Inject
    public ContestProblemStore(ContestProblemDao problemDao) {
        this.problemDao = problemDao;
    }

    public Optional<ContestProblem> findProblem(String contestJid, String problemJid) {
        return problemDao.selectByContestJidAndProblemJid(contestJid, problemJid)
                .map(ContestProblemStore::fromModel);
    }

    public Optional<ContestProblem> findProblemByAlias(String contestJid, String problemAlias) {
        return problemDao.selectByContestJidAndProblemAlias(contestJid, problemAlias)
                .map(ContestProblemStore::fromModel);
    }

    public Set<String> getOpenProblemJids(String contestJid) {
        return problemDao.selectAllOpenByContestJid(contestJid)
                .stream()
                .map(model -> model.problemJid)
                .collect(Collectors.toSet());
    }

    public List<ContestProblem> getProblems(String contestJid) {
        return problemDao.selectAllByContestJid(contestJid)
                .stream()
                .map(ContestProblemStore::fromModel)
                .collect(Collectors.toList());
    }

    public Map<String, String> findProblemAliasesByJids(String contestJid, Set<String> problemJids) {
        Map<String, String> problemAliases = problemDao.selectAllByContestJid(contestJid)
                .stream()
                .collect(Collectors.toMap(m -> m.problemJid, m -> m.alias));
        return problemJids
                .stream()
                .collect(Collectors.toMap(jid -> jid, problemAliases::get));
    }

    private static ContestProblem fromModel(ContestProblemModel model) {
        return new ContestProblem.Builder()
                .problemJid(model.problemJid)
                .alias(model.alias)
                .status(ContestProblemStatus.valueOf(model.status))
                .submissionsLimit(model.submissionsLimit)
                .build();
    }
}
