# coding: utf-8

"""
    OpenAPI Petstore

    This spec is mainly for testing Petstore server and contains fake endpoints, models. Please do not use this for any other purpose. Special characters: \" \\

    The version of the OpenAPI document: 1.0.0
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import unittest

from petstore_api.api.store_api import StoreApi


class TestStoreApi(unittest.TestCase):
    """StoreApi unit test stubs"""

    def setUp(self) -> None:
        self.api = StoreApi()

    def tearDown(self) -> None:
        pass

    def test_delete_order(self) -> None:
        """Test case for delete_order

        Delete purchase order by ID
        """
        pass

    def test_get_inventory(self) -> None:
        """Test case for get_inventory

        Returns pet inventories by status
        """
        pass

    def test_get_order_by_id(self) -> None:
        """Test case for get_order_by_id

        Find purchase order by ID
        """
        pass

    def test_place_order(self) -> None:
        """Test case for place_order

        Place an order for a pet
        """
        pass


if __name__ == '__main__':
    unittest.main()
