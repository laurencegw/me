import Vue from "vue"
import Vuex from "vuex"
import {UserStore} from "@/users/store"
import {MyContentStore} from "@/mycontent/store"
import {ArticlesStore} from "@/stores/ArticlesStore"
import {FrontPageStore} from "@/frontpage/store"

Vue.use(Vuex)

const store = new Vuex.Store({
    modules: {
        UserStore,
        MyContentStore,
        ArticlesStore,
        FrontPageStore
    }
})

export default store
