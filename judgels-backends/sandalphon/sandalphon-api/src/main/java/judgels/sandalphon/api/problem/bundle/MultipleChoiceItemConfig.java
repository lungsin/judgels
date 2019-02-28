package judgels.sandalphon.api.problem.bundle;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableMultipleChoiceItemConfig.class)
public interface MultipleChoiceItemConfig extends ItemConfig {
    int getScore();
    int getPenalty();
    List<Choice> getChoices();

    @Value.Immutable
    @JsonDeserialize(as = ImmutableChoice.class)
    interface Choice {
        String getAlias();
        String getContent();
        boolean getIsCorrect();

        class Builder extends ImmutableChoice.Builder {}
    }

    class Builder extends ImmutableMultipleChoiceItemConfig.Builder {}
}