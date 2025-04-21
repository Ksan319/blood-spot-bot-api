package com.pet_projects.bloodspotbotapi.bot.keyboard;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class CustomKeyBoardBuilder {

    private final List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
    private List<InlineKeyboardButton> currentRow = new ArrayList<>();

    public static CustomKeyBoardBuilder builder() {
        return new CustomKeyBoardBuilder();
    }

    public CustomKeyBoardBuilder row() {
        if (!currentRow.isEmpty()) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.addAll(currentRow);
            keyboardRows.add(row);
            currentRow = new ArrayList<>();
        }
        return this;
    }

    public CustomKeyBoardBuilder button(String text, String callback) {
        InlineKeyboardButton inlineKeyboardButton =
                InlineKeyboardButton.builder()
                        .text(text)
                        .build();
        if (callback.startsWith("https:")){
            inlineKeyboardButton.setUrl(callback);
        } else {
            inlineKeyboardButton.setCallbackData(callback);
        }
        currentRow.add(inlineKeyboardButton);
        return this;
    }

    public InlineKeyboardMarkup build() {
        this.row();
        return new InlineKeyboardMarkup(keyboardRows);
    }

}
