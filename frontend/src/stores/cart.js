import { writable, derived } from 'svelte/store';

function createCartStore() {
  const { subscribe, set, update } = writable({
    storeId: null,
    storeName: '',
    orderDate: null,
    items: []
  });

  return {
    subscribe,
    addItem: (inventory) => {
      update(cart => {
        const existing = cart.items.find(i => i.inventoryId === inventory.id);
        if (existing) {
          existing.quantity += 1;
        } else {
          cart.items.push({
            inventoryId: inventory.id,
            productName: inventory.productName,
            unit: inventory.unit,
            price: inventory.price,
            quantity: 1,
            maxQuantity: inventory.remainingQuantity
          });
        }
        return cart;
      });
    },
    updateQuantity: (inventoryId, quantity) => {
      update(cart => {
        const item = cart.items.find(i => i.inventoryId === inventoryId);
        if (item) {
          item.quantity = Math.max(1, Math.min(quantity, item.maxQuantity));
        }
        return cart;
      });
    },
    removeItem: (inventoryId) => {
      update(cart => {
        cart.items = cart.items.filter(i => i.inventoryId !== inventoryId);
        return cart;
      });
    },
    setStore: (storeId, storeName, orderDate) => {
      update(cart => {
        if (cart.storeId !== storeId || cart.orderDate !== orderDate) {
          return { storeId, storeName, orderDate, items: [] };
        }
        return { ...cart, storeId, storeName, orderDate };
      });
    },
    clear: () => set({ storeId: null, storeName: '', orderDate: null, items: [] })
  };
}

export const cart = createCartStore();

export const cartTotal = derived(cart, $cart =>
  $cart.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
);

export const cartItemCount = derived(cart, $cart =>
  $cart.items.reduce((sum, item) => sum + item.quantity, 0)
);
