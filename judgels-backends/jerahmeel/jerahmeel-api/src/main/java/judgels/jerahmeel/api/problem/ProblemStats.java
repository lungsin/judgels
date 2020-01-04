package judgels.jerahmeel.api.problem;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableProblemStats.class)
public interface ProblemStats {
    long getTotalUsersAccepted();
    long getTotalUsersTried();

    class Builder extends ImmutableProblemStats.Builder {}
}
