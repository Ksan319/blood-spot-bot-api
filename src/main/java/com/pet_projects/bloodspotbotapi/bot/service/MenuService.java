package com.pet_projects.bloodspotbotapi.bot.service;

import com.pet_projects.bloodspotbotapi.bot.configuration.BotMenuProperties;
import com.pet_projects.bloodspotbotapi.bot.keyboard.CustomKeyBoardBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final BotMenuProperties props;

    public String getText(String key, Object... args){
        BotMenuProperties.Menu menu = props.getMenus().get(key);
        return String.format(menu.getText(), args);
    }

    public InlineKeyboardMarkup getButtons(String key, Object... args){
        CustomKeyBoardBuilder customKeyBoardBuilder = new CustomKeyBoardBuilder();
        BotMenuProperties.Menu menu = props.getMenus().get(key);
        if (menu.getButtons() != null) {
            for (BotMenuProperties.Button button : menu.getButtons()) {
                String callback = button.getCallBack();
                try {
                    callback = String.format(callback, args);
                } catch (Exception ignored) {
                    // если форматирование не требуется или формат некорректен — оставляем как есть
                }
                if (callback.equals("ROW")){
                    customKeyBoardBuilder.row();
                }  else {
                    customKeyBoardBuilder.button(button.getLabel(), callback);
                }
            }
        }
        return customKeyBoardBuilder.build();
    }
}
