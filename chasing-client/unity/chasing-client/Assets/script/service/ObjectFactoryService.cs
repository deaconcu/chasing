
using UnityEngine;
using UnityEngine.UI;
#if UNITY_EDITOR
using UnityEditor;
#endif

public class ObjectFactoryService
{

    public static GameObject createText(GameObject parentObject, string name, string fontType,
        int fontSize, Color color, string content, int posX, int posY, int sizeX, int sizeY)
    {
        return createText(parentObject, name, fontType, fontSize, color, content, posX, posY, sizeX, sizeY, TextAnchor.UpperLeft);
    }

    public static GameObject createText(GameObject parentObject, string name, string fontType,
        int fontSize, Color color, string content, int posX, int posY, int sizeX, int sizeY, TextAnchor textAnchor)
    {
        GameObject gameObject = new GameObject(name);
        gameObject.transform.SetParent(parentObject.transform);
        RectTransform transform = gameObject.AddComponent<RectTransform>();

        transform.sizeDelta = new Vector2(sizeX, sizeY);
        transform.localPosition = new Vector2(posX, posY);
        transform.pivot = new Vector2(0, 1);

        Text text = gameObject.AddComponent<Text>();
        if (("arial").Equals(fontType))
        {
            Font ArialFont = (Font)Resources.GetBuiltinResource(typeof(Font), "Arial.ttf");
            text.font = ArialFont;
        }
        else
        {
            Font ArialFont = (Font)Resources.GetBuiltinResource(typeof(Font), "Arial.ttf");
            text.font = ArialFont;
        }
        text.text = content;
        text.fontSize = fontSize;
        text.color = color;
        text.alignment = textAnchor;
        return gameObject;
    }

    public static GameObject createButton(GameObject parentObject, string name, string fontType,
        int fontSize, string title, int posX, int posY, int sizeX, int sizeY)
    {
        GameObject gameObject = new GameObject(name);
        gameObject.transform.SetParent(parentObject.transform);
        RectTransform transform = gameObject.AddComponent<RectTransform>();

        transform.sizeDelta = new Vector2(sizeX, sizeY);
        transform.localPosition = new Vector2(posX, posY);
        transform.pivot = new Vector2(0, 1);

        Image image = gameObject.AddComponent<Image>();
        Sprite sprite = null;
#if UNITY_EDITOR
        sprite = AssetDatabase.GetBuiltinExtraResource<Sprite>("UI/Skin/UISprite.psd");
#endif
        image.sprite = sprite;
        image.type = Image.Type.Sliced;

        Button button = gameObject.AddComponent<Button>();
        button.transition = Button.Transition.ColorTint;

        ObjectFactoryService.createText(
            gameObject, "name", "arial", 16, Color.black, title, 0, 0, sizeX, sizeY, TextAnchor.MiddleCenter);
        return gameObject;
    }


}