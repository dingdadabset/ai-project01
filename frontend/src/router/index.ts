import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/HomeView.vue'),
      meta: { title: 'Home' }
    },
    {
      path: '/posts',
      name: 'posts',
      component: () => import('@/views/posts/PostList.vue'),
      meta: { title: 'Posts' }
    },
    {
      path: '/posts/:slug',
      name: 'post-detail',
      component: () => import('@/views/posts/PostDetail.vue'),
      meta: { title: 'Post' }
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/categories/CategoryList.vue'),
      meta: { title: 'Categories' }
    },
    {
      path: '/categories/:id',
      name: 'category-posts',
      component: () => import('@/views/categories/CategoryPosts.vue'),
      meta: { title: 'Category' }
    },
    {
      path: '/tags',
      name: 'tags',
      component: () => import('@/views/tags/TagList.vue'),
      meta: { title: 'Tags' }
    },
    {
      path: '/tags/:slug',
      name: 'tag-posts',
      component: () => import('@/views/tags/TagPosts.vue'),
      meta: { title: 'Tag' }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/pages/AboutPage.vue'),
      meta: { title: 'About' }
    },
    {
      path: '/tools',
      name: 'tools',
      component: () => import('@/views/tools/ExternalTools.vue'),
      meta: { title: 'External Tools' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/admin/LoginPage.vue'),
      meta: { title: 'Login' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { title: 'Admin' },
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: 'posts',
          name: 'admin-posts',
          component: () => import('@/views/admin/PostManager.vue')
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/CategoryManager.vue')
        },
        {
          path: 'tags',
          name: 'admin-tags',
          component: () => import('@/views/admin/TagManager.vue')
        },
        {
          path: 'comments',
          name: 'admin-comments',
          component: () => import('@/views/admin/CommentManager.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('@/views/admin/UserManager.vue')
        },
        {
          path: 'news',
          name: 'admin-news',
          component: () => import('@/views/admin/NewsManager.vue')
        },
        {
          path: 'stocks',
          name: 'admin-stocks',
          component: () => import('@/views/admin/StockManager.vue')
        },
        {
          path: 'themes',
          name: 'admin-themes',
          component: () => import('@/views/admin/ThemeManager.vue')
        }
      ]
    },
    {
      path: '/news',
      name: 'news',
      component: () => import('@/views/news/NewsList.vue'),
      meta: { title: 'Hot News' }
    },
    {
      path: '/news/:id',
      name: 'news-detail',
      component: () => import('@/views/news/NewsDetail.vue'),
      meta: { title: 'News' }
    },
    {
      path: '/stocks',
      name: 'stocks',
      component: () => import('@/views/stocks/StockList.vue'),
      meta: { title: 'Stock Market' }
    }
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// Page title update
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || 'Blog'} - Halo Blog`
  next()
})

export default router
