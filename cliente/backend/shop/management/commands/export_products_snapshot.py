from django.core.management.base import BaseCommand
from shop.catalog_snapshot import export_snapshot, snapshot_path


class Command(BaseCommand):
    help = "Generate products catalog snapshot JSON for chatbot consumption"

    def add_arguments(self, parser):
        parser.add_argument('--silent', action='store_true', help='Suppress output path printing')

    def handle(self, *args, **options):
        path = export_snapshot()
        if not options.get('silent'):
            self.stdout.write(self.style.SUCCESS(f"Snapshot written to {path}"))