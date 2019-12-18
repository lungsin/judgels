package judgels.jerahmeel.chapter.submission.bundle;

import static java.util.stream.Collectors.toSet;
import static judgels.service.ServiceUtils.checkAllowed;
import static judgels.service.ServiceUtils.checkFound;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.hibernate.UnitOfWork;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import judgels.jerahmeel.api.chapter.problem.ChapterProblem;
import judgels.jerahmeel.api.chapter.submission.bundle.ChapterItemSubmissionService;
import judgels.jerahmeel.api.submission.SubmissionConfig;
import judgels.jerahmeel.api.submission.bundle.ItemSubmissionsResponse;
import judgels.jerahmeel.api.submission.bundle.SubmissionSummaryResponse;
import judgels.jerahmeel.chapter.ChapterStore;
import judgels.jerahmeel.chapter.problem.ChapterProblemStore;
import judgels.jerahmeel.submission.SubmissionRoleChecker;
import judgels.jophiel.api.profile.Profile;
import judgels.jophiel.api.profile.ProfileService;
import judgels.jophiel.api.user.search.UserSearchService;
import judgels.persistence.api.Page;
import judgels.sandalphon.api.problem.bundle.Item;
import judgels.sandalphon.api.problem.bundle.ItemType;
import judgels.sandalphon.api.problem.bundle.ProblemWorksheet;
import judgels.sandalphon.api.submission.bundle.Grading;
import judgels.sandalphon.api.submission.bundle.ItemSubmission;
import judgels.sandalphon.api.submission.bundle.ItemSubmissionData;
import judgels.sandalphon.problem.ProblemClient;
import judgels.sandalphon.submission.bundle.ItemSubmissionGraderRegistry;
import judgels.sandalphon.submission.bundle.ItemSubmissionRegrader;
import judgels.sandalphon.submission.bundle.ItemSubmissionStore;
import judgels.service.actor.ActorChecker;
import judgels.service.api.actor.AuthHeader;

public class ChapterItemSubmissionResource implements ChapterItemSubmissionService {
    private final ActorChecker actorChecker;
    private final ChapterStore chapterStore;
    private final ItemSubmissionStore submissionStore;
    private final SubmissionRoleChecker submissionRoleChecker;
    private final ChapterProblemStore problemStore;
    private final ProfileService profileService;
    private final UserSearchService userSearchService;
    private final ItemSubmissionGraderRegistry itemSubmissionGraderRegistry;
    private final ItemSubmissionRegrader itemSubmissionRegrader;
    private final ProblemClient problemClient;

    @Inject
    public ChapterItemSubmissionResource(
            ActorChecker actorChecker,
            ChapterStore chapterStore,
            ItemSubmissionStore submissionStore,
            SubmissionRoleChecker submissionRoleChecker,
            ChapterProblemStore problemStore,
            ProfileService profileService,
            UserSearchService userSearchService,
            ItemSubmissionGraderRegistry itemSubmissionGraderRegistry,
            ItemSubmissionRegrader itemSubmissionRegrader,
            ProblemClient problemClient) {

        this.actorChecker = actorChecker;
        this.chapterStore = chapterStore;
        this.submissionStore = submissionStore;
        this.submissionRoleChecker = submissionRoleChecker;
        this.problemStore = problemStore;
        this.profileService = profileService;
        this.userSearchService = userSearchService;
        this.itemSubmissionGraderRegistry = itemSubmissionGraderRegistry;
        this.itemSubmissionRegrader = itemSubmissionRegrader;
        this.problemClient = problemClient;
    }

    @Override
    @UnitOfWork
    public ItemSubmissionsResponse getSubmissions(
            Optional<AuthHeader> authHeader,
            String chapterJid,
            Optional<String> username,
            Optional<String> problemAlias,
            Optional<Integer> page) {

        String actorJid = actorChecker.check(authHeader);
        checkFound(chapterStore.getChapterByJid(chapterJid));

        boolean canManage = submissionRoleChecker.canManage(actorJid);
        Optional<String> userJid = username.map(
                u -> userSearchService.translateUsernamesToJids(ImmutableSet.of(u)).getOrDefault(u, ""));

        Optional<String> problemJid = Optional.empty();
        if (problemAlias.isPresent()) {
            Optional<ChapterProblem> problem = problemStore.getProblemByAlias(chapterJid, problemAlias.get());
            problemJid = Optional.ofNullable(problem.isPresent() ? problem.get().getProblemJid() : null);
        }

        Page<ItemSubmission> submissions = submissionStore.getSubmissions(chapterJid, userJid, problemJid, page);

        Set<String> userJids = submissions.getPage().stream().map(ItemSubmission::getUserJid).collect(toSet());
        Set<String> problemJids = submissions.getPage().stream().map(ItemSubmission::getProblemJid).collect(toSet());

        Map<String, Profile> profilesMap = userJids.isEmpty()
                ? Collections.emptyMap()
                : profileService.getProfiles(userJids);

        SubmissionConfig config = new SubmissionConfig.Builder()
                .canManage(canManage)
                .problemJids(problemJids)
                .build();

        Map<String, String> problemAliasesMap = problemStore.getProblemAliasesByJids(problemJids);

        Set<String> itemJids = submissions.getPage().stream()
                .map(ItemSubmission::getItemJid)
                .collect(toSet());

        Map<String, Item> itemsMap = problemClient.getItems(problemJids, itemJids);
        Map<String, Integer> itemNumbersMap = itemsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getNumber().orElse(0)));
        Map<String, ItemType> itemTypesMap = itemsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getType()));

        return new ItemSubmissionsResponse.Builder()
                .data(submissions)
                .config(config)
                .profilesMap(profilesMap)
                .problemAliasesMap(problemAliasesMap)
                .itemNumbersMap(itemNumbersMap)
                .itemTypesMap(itemTypesMap)
                .build();
    }

    @Override
    @UnitOfWork
    public void createItemSubmission(AuthHeader authHeader, ItemSubmissionData data) {
        String actorJid = actorChecker.check(authHeader);
        checkFound(chapterStore.getChapterByJid(data.getContainerJid()));
        checkFound(problemStore.getProblem(data.getProblemJid()));

        Item item = checkFound(problemClient.getItem(data.getProblemJid(), data.getItemJid()));

        if (data.getAnswer().trim().isEmpty()) {
            submissionStore.deleteSubmission(data.getContainerJid(), data.getProblemJid(), data.getItemJid(), actorJid);
        } else {
            Grading grading = itemSubmissionGraderRegistry
                    .get(item.getType())
                    .grade(item, data.getAnswer());

            submissionStore.upsertSubmission(
                    data.getContainerJid(),
                    data.getProblemJid(),
                    data.getItemJid(),
                    data.getAnswer(),
                    grading,
                    actorJid);
        }
    }

    @Override
    @UnitOfWork(readOnly = true)
    public Map<String, ItemSubmission> getLatestSubmissions(
            AuthHeader authHeader,
            String chapterJid,
            Optional<String> username,
            String problemAlias) {

        String actorJid = actorChecker.check(authHeader);
        checkFound(chapterStore.getChapterByJid(chapterJid));

        boolean canManage = submissionRoleChecker.canManage(actorJid);
        String userJid;
        if (canManage && username.isPresent()) {
            userJid = checkFound(Optional.ofNullable(
                    userSearchService.translateUsernamesToJids(ImmutableSet.of(username.get())).get(username.get())));
        } else {
            userJid = actorJid;
        }

        ChapterProblem problem = checkFound(problemStore.getProblemByAlias(chapterJid, problemAlias));

        List<ItemSubmission> submissions = submissionStore.getLatestSubmissionsByUserForProblemInContainer(
                chapterJid,
                problem.getProblemJid(),
                userJid);

        return submissions.stream()
                .map(ItemSubmission::withoutGrading)
                .collect(Collectors.toMap(ItemSubmission::getItemJid, Function.identity()));
    }

    @Override
    @UnitOfWork(readOnly = true)
    public SubmissionSummaryResponse getSubmissionSummary(
            AuthHeader authHeader,
            String chapterJid,
            Optional<String> username,
            Optional<String> language) {

        String actorJid = actorChecker.check(authHeader);
        checkFound(chapterStore.getChapterByJid(chapterJid));

        boolean canManage = submissionRoleChecker.canManage(actorJid);
        String userJid;
        if (canManage && username.isPresent()) {
            userJid = checkFound(Optional.ofNullable(
                    userSearchService.translateUsernamesToJids(ImmutableSet.of(username.get())).get(username.get())));
        } else {
            userJid = actorJid;
        }

        List<? extends ItemSubmission> submissions =
                submissionStore.getLatestSubmissionsByUserInContainer(chapterJid, userJid);

        Map<String, ItemSubmission> submissionsByItemJid = submissions.stream()
                .collect(Collectors.toMap(ItemSubmission::getItemJid, Function.identity()));

        List<String> problemJidsSortedByAlias = problemStore.getBundleProblemJids(chapterJid);
        Map<String, String> problemAliasesByProblemJid =
                problemStore.getProblemAliasesByJids(ImmutableSet.copyOf(problemJidsSortedByAlias));

        Map<String, List<String>> itemJidsByProblemJid = new HashMap<>();
        Map<String, ItemType> itemTypesByItemJid = new HashMap<>();
        for (String problemJid : problemJidsSortedByAlias) {
            ProblemWorksheet worksheet = problemClient.getBundleProblemWorksheet(problemJid, language);
            List<Item> items = worksheet.getItems().stream()
                    .filter(item -> !item.getType().equals(ItemType.STATEMENT))
                    .collect(Collectors.toList());
            items.sort(Comparator.comparingInt(item -> item.getNumber().get()));

            items.forEach(item -> itemTypesByItemJid.put(item.getJid(), item.getType()));

            itemJidsByProblemJid.put(
                    problemJid,
                    items.stream().map(Item::getJid).collect(Collectors.toList()));
        }

        Map<String, String> problemNamesByProblemJid =
                problemClient.getProblemNames(ImmutableSet.copyOf(problemJidsSortedByAlias), language);

        Profile profile = profileService.getProfile(userJid);

        SubmissionConfig config = new SubmissionConfig.Builder()
                .canManage(canManage)
                .userJids(ImmutableList.of(userJid))
                .problemJids(problemJidsSortedByAlias)
                .build();

        return new SubmissionSummaryResponse.Builder()
                .profile(profile)
                .config(config)
                .itemJidsByProblemJid(itemJidsByProblemJid)
                .submissionsByItemJid(submissionsByItemJid)
                .itemTypesMap(itemTypesByItemJid)
                .problemAliasesMap(problemAliasesByProblemJid)
                .problemNamesMap(problemNamesByProblemJid)
                .build();
    }

    @Override
    @UnitOfWork
    public void regradeSubmission(AuthHeader authHeader, String submissionJid) {
        String actorJid = actorChecker.check(authHeader);
        ItemSubmission submission = checkFound(submissionStore.getSubmissionByJid(submissionJid));
        checkFound(chapterStore.getChapterByJid(submission.getContainerJid()));
        checkAllowed(submissionRoleChecker.canManage(actorJid));

        itemSubmissionRegrader.regradeSubmission(submission);
    }

    @Override
    @UnitOfWork
    public void regradeSubmissions(
            AuthHeader authHeader,
            Optional<String> chapterJid,
            Optional<String> userJid,
            Optional<String> problemJid) {

        String actorJid = actorChecker.check(authHeader);
        checkAllowed(submissionRoleChecker.canManage(actorJid));

        itemSubmissionRegrader.regradeSubmissions(chapterJid, userJid, problemJid);
    }
}