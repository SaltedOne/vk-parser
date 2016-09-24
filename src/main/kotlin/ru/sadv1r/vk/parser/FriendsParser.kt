package ru.sadv1r.vk.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import ru.sadv1r.vk.parser.model.Profile

/**
 * Парсер друзей пользователя Вконтакте
 *
 * Created on 4/3/16.
 *
 * @author sadv1r
 * @version 0.1
 */
class FriendsParser : Parser() {
    private val logger = LoggerFactory.getLogger(ProfileParser::class.java)

    /**
     * Возвращает список идентификаторов друзей пользователя.
     *
     * @param vkId
     *        Идентификатор пользователя, для которого необходимо получить список друзей.
     *        Если параметр не задан, то считается, что он равен идентификатору текущего пользователя
     *        (справедливо для вызова с передачей **access_token**)
     * @return {@code List} идентификаторов (**id**) друзей пользователя
     */
    fun getFriends(vkId: Int): List<Int> {
        val methodName = "friends.get"

        val responseTree = getResponseTree(methodName, "&user_id=$vkId")

        return getFriends(responseTree)
    }

    /**
     * Возвращает список идентификаторов друзей пользователя.
     *
     * @param vkId
     *        Идентификатор пользователя, для которого необходимо получить список друзей.
     *        Если параметр не задан, то считается, что он равен идентификатору текущего пользователя
     *        (справедливо для вызова с передачей **access_token**).
     * @param order
     *        Порядок, в котором нужно вернуть список друзей.
     *        По умолчанию список сортируется в порядке возрастания идентификаторов пользователей
     * @param listId
     *        Идентификатор списка друзей, полученный методом [friends.getLists],
     *        друзей из которого необходимо получить. Данный параметр учитывается,
     *        только когда параметр **user_id** равен идентификатору текущего пользователя.
     * @param count количество друзей, которое нужно вернуть. (по умолчанию – **все друзья**)
     * @param offset смещение, необходимое для выборки определенного подмножества друзей.
     * @param nameCase падеж для склонения имени и фамилии пользователя.
     * @return {@code List} идентификаторов (**id**) друзей пользователя
     */
    fun getFriends(vkId: Int, order: Order? = null, listId: Int? = null, count: Int? = null,
                   offset: Int? = null, nameCase: NameCase? = null): List<Int> {
        val methodName = "friends.get"

        val params = mapOf("user_id" to vkId, "order" to order, "list_id" to listId, "count" to count,
                "offset" to offset, "name_case" to nameCase)

        val responseTree = getResponseTree(methodName, params)

        return getFriends(responseTree)
    }

    /**
     * Возвращает расширенную информацию о друзьях пользователя.
     *
     * @param vkId
     *        Идентификатор пользователя, для которого необходимо получить список друзей.
     *        Если параметр не задан, то считается, что он равен идентификатору текущего пользователя
     *        (справедливо для вызова с передачей **access_token**).
     * @param order
     *        Порядок, в котором нужно вернуть список друзей.
     *        По умолчанию список сортируется в порядке возрастания идентификаторов пользователей
     * @param listId
     *        Идентификатор списка друзей, полученный методом [friends.getLists],
     *        друзей из которого необходимо получить. Данный параметр учитывается,
     *        только когда параметр **user_id** равен идентификатору текущего пользователя.
     * @param count количество друзей, которое нужно вернуть. (по умолчанию – **все друзья**)
     * @param offset смещение, необходимое для выборки определенного подмножества друзей.
     * @param fields
     *        Список дополнительных полей, которые необходимо вернуть.
     *        Доступные значения: *nickname, domain, sex, bdate, city, country, timezone,*
     *        *photo_50, photo_100, photo_200_orig, has_mobile, contacts, education, online,*
     *        *relation, last_seen, status, can_write_private_message, can_see_all_posts, can_post, universities*
     * @param nameCase падеж для склонения имени и фамилии пользователя.
     * @return {@code List} объектов пользователей, но не более 5000.
     */
    fun getFriends(vkId: Int, order: Order? = null, listId: Int? = null, count: Int? = null,
                   offset: Int? = null, fields: String, nameCase: NameCase? = null): List<Profile> {
        val methodName = "friends.get"

        val params = mapOf("user_id" to vkId, "order" to order, "list_id" to listId, "count" to count,
                "offset" to offset, "fields" to fields, "name_case" to nameCase)

        val responseTree = getResponseTree(methodName, params)

        return getFriendsProfiles(responseTree)
    }

    /**
     * @param jsonNode
     *        {@code JsonNode} с деревом ответа метода Вконтакте [friends.get][getFriends]
     * @return {@code List} друзей пользователя
     */
    private fun getFriends(jsonNode: JsonNode): List<Int> {
        val result: List<Int> = jacksonObjectMapper()
                .readValue(jsonNode.get("response").get("items").toString())

        logger.trace("Получен профиль: {}", result)

        return result
    }

    /**
     * @param jsonNode
     *        {@code JsonNode} с деревом ответа метода Вконтакте [friends.get][getFriends]
     * @return {@code List} друзей пользователя
     */
    private fun getFriendsProfiles(jsonNode: JsonNode): List<Profile> {
        val result: List<Profile> = jacksonObjectMapper()
                .readValue(jsonNode.get("response").get("items").toString())

        logger.trace("Получен профиль: {}", result)

        return result
    }

    /**
     * @property HINTS сортировать по рейтингу, аналогично тому, как друзья сортируются в разделе **Мои друзья**
     * @property RANDOM возвращает друзей в случайном порядке
     * @property MOBILE возвращает выше тех друзей, у которых установлены мобильные приложения
     * @property NAME
     *           Сортировать по имени. Данный тип сортировки **работает медленно**,
     *           так как сервер будет получать всех друзей а не только указанное количество **count**.
     *           (работает только при переданном параметре **fields**)
     */
    enum class Order {
        HINTS,
        RANDOM,
        MOBILE,
        NAME;

        override fun toString(): String = this.name.toLowerCase()
    }

    /**
     * @property NOM именительный
     * @property GEN родительный
     * @property DAT дательный
     * @property ACC винительный
     * @property INS творительный
     * @property ABL предложный
     */
    enum class NameCase {
        NOM,
        GEN,
        DAT,
        ACC,
        INS,
        ABL;

        override fun toString(): String = this.name.toLowerCase()
    }
}
