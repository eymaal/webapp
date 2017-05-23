package org.literacyapp.model.analytics;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import org.literacyapp.model.content.Letter;

@Entity
public class LetterLearningEvent extends LearningEvent {
    
    @NotNull
    private Letter letter;

    public Letter getLetter() {
        return letter;
    }

    public void setLetter(Letter letter) {
        this.letter = letter;
    }
}
