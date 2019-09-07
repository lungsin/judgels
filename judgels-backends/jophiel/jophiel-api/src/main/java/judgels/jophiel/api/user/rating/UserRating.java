package judgels.jophiel.api.user.rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableUserRating.class)
public interface UserRating {
    int INITIAL_RATING = 1800;

    int getPublicRating();
    int getHiddenRating();

    class Builder extends ImmutableUserRating.Builder {}
}
